package de.oglimmer.picz.service;

import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
@ConditionalOnProperty(
        value = "picz.imageStorageImplementation",
        havingValue = "ImageStorageS3")
public class ImageStorageS3 implements ImageStorage {

    private final AppConfig appConfig;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        s3Client = S3Client.builder().credentialsProvider(EnvironmentVariableCredentialsProvider.create()).region(Region.EU_CENTRAL_1).build();
    }

    @SneakyThrows
    @Scheduled(fixedRate = 5000)
    public void deleteCache() {
        FileTime cutoff = FileTime.from(Instant.now().minus(Duration.ofDays(7)));
        for (Path dir : Arrays.asList(
                Paths.get(appConfig.getImagePath()),
                Paths.get(appConfig.getSmallImagePath())
        )) {
            try {
                try (Stream<Path> list = Files.list(dir)) {
                    list.filter(path -> {
                        try {
                            return !path.getFileName().toString().equals(ImageCreateService.OUTOFSPACE_JPG) &&
                                    Files.getLastModifiedTime(path).compareTo(cutoff) < 0;
                        } catch (IOException e) {
                            log.warn("Error getting last modified time of {}", path, e);
                            throw new UncheckedIOException(e);
                        }
                    }).forEach(path -> {
                        log.info("Deleting cached file {}", path);
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("Error deleting cached file {}", path, e);
                            throw new UncheckedIOException(e);
                        }
                    });
                }
            } catch (IOException e) {
                log.warn("Error listing cached files in {}", dir, e);
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public long deletePersistentImagePath(String filename) {
        String objectPath = appConfig.getS3ImagePath() + filename;
        long size = s3Client.headObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(objectPath)).contentLength();
        s3Client.deleteObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(objectPath));
        File file = new File(appConfig.getImagePath(), filename);
        if (file.exists()) {
            file.delete();
        }
        return size;
    }

    @Override
    public long deletePersistentSmallImagePath(String filename) {
        String objectPath = appConfig.getS3SmallImagePath() + filename;
        long size = 0;
        try {
            size = s3Client.headObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(objectPath)).contentLength();
            s3Client.deleteObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(objectPath));
        } catch (NoSuchKeyException e) {
            log.warn("No such key in S3: {}", objectPath);
        }
        File file = new File(appConfig.getSmallImagePath(), filename);
        if (file.exists()) {
            file.delete();
        }
        return size;
    }

    @Override
    public void transferToPersistentStorage(Path imagePath, Path smallPath) {
        s3Client.putObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(appConfig.getS3ImagePath() + imagePath.getFileName().toString()).build(), imagePath);
        s3Client.putObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(appConfig.getS3SmallImagePath() + smallPath.getFileName().toString()).build(), smallPath);
    }

    @Override
    public Path loadFromPersistent(String filename, boolean small, User user) {
        String basePath = small ? appConfig.getSmallImagePath() : appConfig.getImagePath();
        Path path = Paths.get(basePath + filename);
        if (!path.toFile().exists()) {
            try {
                Path imagePath = Paths.get(appConfig.getImagePath(), filename);
                s3Client.getObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(appConfig.getS3ImagePath() + filename), imagePath);
                Path smallImagePath = Paths.get(appConfig.getSmallImagePath(), filename);
                s3Client.getObject(builder -> builder.bucket(appConfig.getS3Bucket()).key(appConfig.getS3SmallImagePath() + filename), smallImagePath);
            } catch (NoSuchKeyException e) {
                log.warn("No such key in S3: {}", appConfig.getS3ImagePath() + filename);
                throw e;
            }
        }
        return path;
    }

}

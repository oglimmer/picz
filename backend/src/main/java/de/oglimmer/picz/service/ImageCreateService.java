package de.oglimmer.picz.service;

import com.google.common.io.ByteStreams;
import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.Album;
import de.oglimmer.picz.util.queue.GenericJobEnqueuer;
import de.oglimmer.picz.web.dto.UploadedImage;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@AllArgsConstructor
@Service
@Slf4j
public class ImageCreateService {

    public static final String OUTOFSPACE_JPG = "outofspace.jpg";
    public static final String OUTOFSPACE_SMALL_JPG = "outofspace-small.jpg";

    public static final int PREVIEW_IMAGE_WIDTH = 600;

    private final AppConfig appConfig;
    private final DuplicatedHashService duplicatedHashService;
    private final AlbumRepositoryService albumRepositoryService;
    private final QueueCountStats queueCountStats;
    private final GenericJobEnqueuer jobEnqueuer;
    private final TemporaryFileService temporaryFileService;

    @PostConstruct
    public void init() {
        if (!Files.exists(Paths.get(appConfig.getOriginalImagePath()))) {
            new File(appConfig.getOriginalImagePath()).mkdirs();
        }
        File smallImagePath = new File(appConfig.getSmallImagePath());
        if (!smallImagePath.exists()) {
            smallImagePath.mkdirs();
        }
        File imagePath = new File(appConfig.getImagePath());
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }
        extractFile();
    }

    private void extractFile() {
        try (InputStream inputStream = ImageCreateService.class.getResourceAsStream("/" + OUTOFSPACE_JPG);
             OutputStream outputStream = new FileOutputStream(Path.of(appConfig.getImagePath(), OUTOFSPACE_JPG).toFile())) {
            ByteStreams.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Error copying file", e);
        }
        try (InputStream inputStream = ImageCreateService.class.getResourceAsStream("/" + OUTOFSPACE_SMALL_JPG);
             OutputStream outputStream = new FileOutputStream(Path.of(appConfig.getSmallImagePath(), OUTOFSPACE_SMALL_JPG).toFile())) {
            ByteStreams.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Error copying file", e);
        }
    }

    @SneakyThrows
    public UploadedImage create(MultipartFile image, long albumId) {
        Album album = albumRepositoryService.get(albumId);// check album is owned by user
        UploadedImage uploadedImage = new UploadedImage(albumId);
        String ext = getFileExtensionFromContentType(image);
        uploadedImage.setOriginalFileExtension(ext);
        uploadedImage.setConvertedFileExtension("heic".equals(ext) ? "jpg" : ext); // this needs to be here and not in uploadedImageToAlbumService.createImages(uploadedImage);
        saveFileToDisk(image, uploadedImage);
        if (checkForDuplicatedHash(uploadedImage)) {
            deleteFileFromDisk(uploadedImage);
            throw new UploadException(HttpStatus.ALREADY_REPORTED);
        } else {
            temporaryFileService.save(uploadedImage.getFilename(), Files.readAllBytes(uploadedImage.getOriginalImagePath(appConfig)));
            queueCountStats.incrementProcessingCounter(albumId, album.getUser().getId());
            jobEnqueuer.enqueueImmediately(
                    "imageCreationEntryService",
                    "createImageEntry",
                    new String[]{"de.oglimmer.picz.web.dto.UploadedImage"},
                    new Object[]{uploadedImage},
                    "PARALLEL_SCHEDULER"
            );
            return uploadedImage;
        }
    }


    private static String getFileExtensionFromContentType(MultipartFile image) {
        return switch (Objects.requireNonNull(image.getContentType())) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/heic" -> "heic";
            default -> throw new RuntimeException("Unknown file type: " + image.getContentType());
        };
    }

    private boolean checkForDuplicatedHash(UploadedImage uploadedImage) {
        return duplicatedHashService.checkForDuplicatedHash(uploadedImage);
    }

    private void deleteFileFromDisk(UploadedImage uploadedImage) {
        uploadedImage.getOriginalImagePath(appConfig).toFile().delete();
    }

    private void saveFileToDisk(MultipartFile image, UploadedImage uploadedImage) throws IOException {
        Path outputPath = uploadedImage.getOriginalImagePath(appConfig);
        Files.copy(image.getInputStream(), outputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}

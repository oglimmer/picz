/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web;

import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.db.AlbumElementRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@AllArgsConstructor
@RestController
public class MigrationController {

  private AlbumElementRepository albumElementRepository;
  private AppConfig appConfig;

  @Operation(
      summary = "Migrates images from a local directory to AWS s3",
      security = @SecurityRequirement(name = "OpenID"))
  @GetMapping("/api/v1/migrate")
  public void migrate() {
    try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
      try (S3Client s3Client =
          S3Client.builder()
              .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
              .region(Region.EU_CENTRAL_1)
              .build()) {

        List<AlbumElement> allElements = albumElementRepository.findAll();

        List<CompletableFuture<Void>> futures =
            allElements.stream()
                .filter(e -> e.getFilename() != null && !e.getFilename().isBlank())
                .flatMap(
                    e ->
                        Stream.of(
                            CompletableFuture.runAsync(
                                () -> {
                                  Path imagePath =
                                      Path.of(appConfig.getImagePath(), e.getFilename());
                                  s3Client.putObject(
                                      req ->
                                          req.bucket(appConfig.getS3Bucket())
                                              .key(
                                                  appConfig.getS3ImagePath()
                                                      + imagePath.getFileName()),
                                      imagePath);
                                },
                                executor),
                            CompletableFuture.runAsync(
                                () -> {
                                  Path smallImagePath =
                                      Path.of(appConfig.getSmallImagePath(), e.getFilename());
                                  s3Client.putObject(
                                      req ->
                                          req.bucket(appConfig.getS3Bucket())
                                              .key(
                                                  appConfig.getS3SmallImagePath()
                                                      + smallImagePath.getFileName()),
                                      smallImagePath);
                                },
                                executor)))
                .toList();

        // Wait for all uploads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
      }
    }
  }
}

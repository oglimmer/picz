/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web.dto;

import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.util.RandomData;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class UploadedImage {

  private String filename = RandomData.generateRandomString(32);

  private String originalFileExtension; // file extension given my the upload client

  private String convertedFileExtension; // file extension returned to the client

  private String contentHash;

  private final long albumId;

  public Path getOriginalImagePath(AppConfig appConfig) {
    if (originalFileExtension == null) throw new RuntimeException();
    return Paths.get(appConfig.getOriginalImagePath() + filename + "." + originalFileExtension);
  }

  public Path getSmallImagePath(AppConfig appConfig) {
    if (convertedFileExtension == null) throw new RuntimeException();
    return Paths.get(appConfig.getSmallImagePath() + filename + "." + convertedFileExtension);
  }

  public Path getImagePath(AppConfig appConfig) {
    if (convertedFileExtension == null) throw new RuntimeException();
    return Paths.get(appConfig.getImagePath() + filename + "." + convertedFileExtension);
  }
}

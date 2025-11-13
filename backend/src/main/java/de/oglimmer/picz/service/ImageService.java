/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import static de.oglimmer.picz.service.ImageCreateService.OUTOFSPACE_JPG;
import static de.oglimmer.picz.service.ImageCreateService.OUTOFSPACE_SMALL_JPG;

import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.util.RandomData;
import de.oglimmer.picz.util.UserContextHolder;
import de.oglimmer.picz.web.dto.ImagePatchRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ImageService {

  private final ImageResizeService imageResizeService;
  private final AlbumRepositoryService albumRepositoryService;
  private final AppConfig appConfig;
  private final ImageStorage imageStorage;

  @SneakyThrows
  public byte[] get(AlbumElement albumElement, boolean small) {
    Path readPath =
        imageStorage.loadFromPersistent(
            albumElement.getFilename(), small, albumElement.getAlbum().getUser());
    if (readPath == null) {
      readPath =
          Paths.get(
              small
                  ? appConfig.getSmallImagePath() + OUTOFSPACE_SMALL_JPG
                  : appConfig.getImagePath() + OUTOFSPACE_JPG);
    }
    return Files.readAllBytes(readPath);
  }

  @SneakyThrows
  public AlbumElement rotate(long id) {
    AlbumElement image = albumRepositoryService.findAlbumElementById(id);
    if (!image.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Not allowed");
    }
    File inputFile = new File(appConfig.getImagePath() + image.getFilename());
    if (!inputFile.exists()) {
      return image;
    }
    BufferedImage inputImage = ImageIO.read(inputFile);
    try {
      int width = inputImage.getWidth();
      int height = inputImage.getHeight();
      log.debug("rotate image {}: {}x{}", image.getFilename(), width, height);

      BufferedImage dest = new BufferedImage(height, width, inputImage.getType());
      try {
        Graphics2D graphics2D = dest.createGraphics();
        try {
          graphics2D.translate((height - width) / 2, (height - width) / 2);
          graphics2D.rotate(Math.PI / 2, height / 2, width / 2);
          graphics2D.drawRenderedImage(inputImage, null);

          image.setSecretId(RandomData.generateRandomString(32));

          File outputFile = new File(appConfig.getImagePath() + image.getFilename());
          ImageIO.write(dest, "JPG", outputFile);
          imageResizeService.resize(
              outputFile.toPath(),
              Path.of(appConfig.getSmallImagePath(), image.getFilename()),
              ImageCreateService.PREVIEW_IMAGE_WIDTH,
              image.getAlbum().getUser().getJpgQuality());

          return image;
        } finally {
          graphics2D.dispose();
        }
      } finally {
        dest.flush();
      }
    } finally {
      inputImage.flush();
    }
  }

  public void patch(long id, ImagePatchRequest imagePatchRequest) {
    AlbumElement image = albumRepositoryService.findAlbumElementById(id);
    if (!image.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Not allowed");
    }
    image.setDescription(imagePatchRequest.getDescription());
  }

  public Optional<AlbumElement> getMeta(String filename) {
    return albumRepositoryService.findAlbumElementByFilename(filename);
  }
}

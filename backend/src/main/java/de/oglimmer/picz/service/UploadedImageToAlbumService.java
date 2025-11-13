/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.*;
import de.oglimmer.picz.util.RandomData;
import de.oglimmer.picz.web.dto.UploadedImage;
import jakarta.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class UploadedImageToAlbumService {

  private final ImageResizeService imageResizeService;
  private final AlbumRepositoryService albumRepositoryService;
  private final AppConfig appConfig;
  private final CapacityService capacityService;
  private final ImageStorage imageStorage;
  private final QueueCountStats queueCountStats;
  private final TemporaryFileService temporaryFileService;

  @Transactional(Transactional.TxType.REQUIRES_NEW)
  @SneakyThrows
  public AlbumElement createImage(UploadedImage uploadedImage) {
    StopWatch stopWatch = new StopWatch(uploadedImage.getFilename());
    stopWatch.start("create image");
    log.debug("create image start:{}", uploadedImage.getFilename());

    if (!uploadedImage.getOriginalImagePath(appConfig).toFile().exists()) {
      byte[] fileContent =
          temporaryFileService
              .findByUniqueId(uploadedImage.getFilename())
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "File not found: " + uploadedImage.getOriginalImagePath(appConfig)));
      Files.write(uploadedImage.getOriginalImagePath(appConfig), fileContent);
    }
    temporaryFileService.deleteByUniqueId(uploadedImage.getFilename());

    if (uploadedImage.getOriginalFileExtension().equalsIgnoreCase("heic")) {
      convertToJpeg(uploadedImage);
      uploadedImage.setOriginalFileExtension("jpg");
    }

    AlbumElement imageEntity = new AlbumElement();
    imageEntity.setSecretId(RandomData.generateRandomString(32));
    imageEntity.setElementType(AlbumElementType.IMAGE);
    imageEntity.setFilename(
        uploadedImage.getFilename() + "." + uploadedImage.getConvertedFileExtension());
    imageEntity.setOrderNo(0L);
    imageEntity.setContentHash(uploadedImage.getContentHash());
    imageEntity = albumRepositoryService.createAlbumElement(imageEntity);

    // get information from original file
    Path originalImagePath = uploadedImage.getOriginalImagePath(appConfig);
    transferExifData(imageEntity, originalImagePath);
    Album album = albumRepositoryService.findById(uploadedImage.getAlbumId()).orElseThrow();
    imageEntity.setAlbum(album);
    album.getAlbumElements().add(imageEntity);

    Path smallPath = createSmallImage(uploadedImage, originalImagePath, album);

    User user = album.getUser();

    // create large image without EXIF data
    Path imagePath = uploadedImage.getImagePath(appConfig);
    if (user.getMaxImageWidth() == 0) {
      imageResizeService.removeExif(originalImagePath, imagePath, user.getJpgQuality());
    } else {
      imageResizeService.resize(
          originalImagePath, imagePath, user.getMaxImageWidth(), user.getJpgQuality());
    }

    // check available space
    long fileSize = imagePath.toFile().length();
    long smallFileSize = smallPath.toFile().length();

    if (capacityService.useCapacity(user.getId(), fileSize + smallFileSize)) {
      imagePath.toFile().delete();
      smallPath.toFile().delete();
      // duplicatedHashService.clearCache(uploadedImage.getContentHash()); // use this
      // if this block prevents calling dateSectionCreation.queue(imageEntity);
    } else {
      imageStorage.transferToPersistentStorage(imagePath, smallPath);
    }
    originalImagePath.toFile().delete();

    stopWatch.stop();
    queueCountStats.decrementProcessingCounter(uploadedImage.getAlbumId(), album.getUser().getId());
    log.debug("create image end:{}, {}", uploadedImage.getFilename(), stopWatch);

    return imageEntity;
  }

  public Path createSmallImage(UploadedImage uploadedImage, Path originalImagePath, Album album) {
    Path smallPath = uploadedImage.getSmallImagePath(appConfig);
    imageResizeService.resize(
        originalImagePath,
        smallPath,
        ImageCreateService.PREVIEW_IMAGE_WIDTH,
        album.getUser().getJpgQuality());
    return smallPath;
  }

  @SneakyThrows
  private void convertToJpeg(UploadedImage uploadedImage) {
    String inputFile = uploadedImage.getOriginalImagePath(appConfig).toString();
    String outputFile =
        Paths.get(
                appConfig.getOriginalImagePath()
                    + uploadedImage.getFilename()
                    + "."
                    + uploadedImage.getConvertedFileExtension())
            .toString();
    String convertCommand = "convert";

    ProcessBuilder processBuilder = new ProcessBuilder(convertCommand, inputFile, outputFile);
    Process process = processBuilder.start();
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("failed to convert heic to jpeg");
    }
    uploadedImage.getOriginalImagePath(appConfig).toFile().delete();
  }

  @SneakyThrows
  private void transferExifData(AlbumElement albumElement, Path sourcePath) {
    Metadata metadata = ImageMetadataReader.readMetadata(sourcePath.toFile());
    GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
    if (gpsDirectory != null) {
      GeoLocation geoLocation = gpsDirectory.getGeoLocation();
      albumElement.setLongitude(geoLocation.getLongitude());
      albumElement.setLatitude(geoLocation.getLatitude());
    }
    ExifSubIFDDirectory exifSubIFDDirectory =
        metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if (exifSubIFDDirectory != null && exifSubIFDDirectory.getDateOriginal() != null) {
      albumElement.setCreationDate(exifSubIFDDirectory.getDateOriginal());
    } else {
      albumElement.setCreationDate(new Date());
    }
    albumElement.setOrderNo(albumElement.getCreationDate().getTime());
    log.debug("set date for " + albumElement.getFilename() + " to " + formatDate(albumElement));
  }

  public static String formatDate(AlbumElement uploadedImage) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return (uploadedImage.getCreationDate() != null
            ? simpleDateFormat.format(uploadedImage.getCreationDate())
            : "null")
        + " / "
        + simpleDateFormat.format(new Date(uploadedImage.getOrderNo()));
  }
}

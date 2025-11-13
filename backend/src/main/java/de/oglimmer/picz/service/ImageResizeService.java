/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class ImageResizeService {

  // replace with https://github.com/coobird/thumbnailator

  record ImageInformation(int orientation, int width, int height) {}

  @SneakyThrows
  private ImageInformation readImageInformation(File imageFile) {
    int orientation;
    Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
    try {
      Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
      orientation = directory != null ? directory.getInt(ExifIFD0Directory.TAG_ORIENTATION) : 1;
    } catch (com.drew.metadata.MetadataException e) {
      orientation = 1;
    }
    JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);
    if (jpegDirectory != null) {
      int width = jpegDirectory.getImageWidth();
      int height = jpegDirectory.getImageHeight();
      return new ImageInformation(orientation, width, height);
    }
    PngDirectory pngDirectory = metadata.getFirstDirectoryOfType(PngDirectory.class);
    if (pngDirectory != null) {
      int width = pngDirectory.getInt(PngDirectory.TAG_IMAGE_WIDTH);
      int height = pngDirectory.getInt(PngDirectory.TAG_IMAGE_HEIGHT);
      return new ImageInformation(orientation, width, height);
    }
    BufferedImage image = ImageIO.read(imageFile);
    try {
      log.debug("Failed to read EXIF, loading image to get width/height: {}", imageFile);
      return new ImageInformation(orientation, image.getWidth(), image.getHeight());
    } finally {
      image.flush();
    }
  }

  // Look at http://chunter.tistory.com/143 for information
  private AffineTransform getExifTransformation(ImageInformation info) {
    AffineTransform affineTransform = new AffineTransform();
    switch (info.orientation) {
      case 1:
        break;
      case 2: // Flip X
        affineTransform.scale(-1.0, 1.0);
        affineTransform.translate(-info.width, 0);
        break;
      case 3: // PI rotation
        affineTransform.translate(info.width, info.height);
        affineTransform.rotate(Math.PI);
        break;
      case 4: // Flip Y
        affineTransform.scale(1.0, -1.0);
        affineTransform.translate(0, -info.height);
        break;
      case 5: // - PI/2 and Flip X
        affineTransform.rotate(-Math.PI / 2);
        affineTransform.scale(-1.0, 1.0);
        break;
      case 6: // -PI/2 and -width
        affineTransform.translate(info.height, 0);
        affineTransform.rotate(Math.PI / 2);
        break;
      case 7: // PI/2 and Flip
        affineTransform.scale(-1.0, 1.0);
        affineTransform.translate(-info.height, 0);
        affineTransform.translate(0, info.width);
        affineTransform.rotate(3 * Math.PI / 2);
        break;
      case 8: // PI / 2
        affineTransform.translate(0, info.width);
        affineTransform.rotate(3 * Math.PI / 2);
        break;
    }
    return affineTransform;
  }

  @SneakyThrows
  public void resize(
      Path sourceImagePath, Path destinationImagePath, int targetWidth, float jpgQuality) {
    // Read the source image
    BufferedImage sourceImage = initImage(sourceImagePath);
    try {
      log.debug("Resizing {} to {}x{}", sourceImagePath, targetWidth, sourceImage.getHeight());
      // Calculate the target height while maintaining the aspect ratio
      int targetHeight =
          (int) ((double) sourceImage.getHeight() / sourceImage.getWidth() * targetWidth);
      // Create a new resized image
      BufferedImage resizedImage =
          new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
      try {
        // Resize the source image to the target dimensions
        resizedImage
            .getGraphics()
            .drawImage(
                sourceImage.getScaledInstance(
                    targetWidth, targetHeight, BufferedImage.SCALE_SMOOTH),
                0,
                0,
                null);
        // Save the resized image to the destination file
        File output = destinationImagePath.toFile();
        writeImage(resizedImage, output, jpgQuality);
      } finally {
        resizedImage.flush();
      }
    } finally {
      sourceImage.flush();
    }
  }

  private static void writeImage(BufferedImage resizedImage, File output, float quality)
      throws IOException {
    if (output.getName().endsWith(".png")) {
      ImageWriter pngWriter = ImageIO.getImageWritersByFormatName("png").next();
      try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(output)) {
        log.debug("Writing image {} with quality {}", output, quality);
        pngWriter.setOutput(outputStream);
        IIOImage outputImage = new IIOImage(resizedImage, null, null);
        pngWriter.write(outputImage);
      } finally {
        pngWriter.dispose();
      }
    } else if (output.getName().endsWith(".jpg") || output.getName().endsWith(".jpeg")) {
      ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
      ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
      jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      jpgWriteParam.setCompressionQuality(quality);

      try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(output)) {
        log.debug("Writing image {} with quality {}", output, quality);
        jpgWriter.setOutput(outputStream);
        IIOImage outputImage = new IIOImage(resizedImage, null, null);
        jpgWriter.write(null, outputImage, jpgWriteParam);
      } finally {
        jpgWriter.dispose();
      }
    } else {
      throw new RuntimeException("Unknown image type: " + output.getName());
    }
  }

  @SneakyThrows
  public void removeExif(Path sourceImagePath, Path destinationImagePath, float jpgQuality) {
    // Read the source image
    BufferedImage sourceImage = initImage(sourceImagePath);
    try {
      log.debug("Removing EXIF from {} to {}", sourceImagePath, destinationImagePath);
      BufferedImage resizedImage =
          new BufferedImage(
              sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_RGB);
      try {
        resizedImage.getGraphics().drawImage(sourceImage, 0, 0, null);

        File output = destinationImagePath.toFile();
        writeImage(resizedImage, output, jpgQuality);
      } finally {
        resizedImage.flush();
      }
    } finally {
      sourceImage.flush();
    }
  }

  private BufferedImage initImage(Path sourceImagePath) throws IOException {
    File input = sourceImagePath.toFile();

    ImageInformation imageInformation = readImageInformation(input);
    BufferedImage sourceImage = ImageIO.read(input);
    AffineTransformOp op =
        new AffineTransformOp(
            getExifTransformation(imageInformation), AffineTransformOp.TYPE_BICUBIC);

    BufferedImage destinationImage =
        op.createCompatibleDestImage(
            sourceImage,
            (sourceImage.getType() == BufferedImage.TYPE_BYTE_GRAY)
                ? sourceImage.getColorModel()
                : null);
    Graphics2D g = destinationImage.createGraphics();
    g.setBackground(Color.WHITE);
    g.clearRect(0, 0, destinationImage.getWidth(), destinationImage.getHeight());
    return op.filter(sourceImage, destinationImage);
  }
}

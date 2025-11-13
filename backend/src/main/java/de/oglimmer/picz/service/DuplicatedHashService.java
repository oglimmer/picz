/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import com.google.common.hash.Hashing;
import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.db.AlbumElementRepository;
import de.oglimmer.picz.web.dto.UploadedImage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class DuplicatedHashService {

  private final AlbumElementRepository albumElementRepository;
  private final AppConfig appConfig;

  @SneakyThrows
  public boolean checkForDuplicatedHash(UploadedImage uploadedImage) {
    String hash =
        com.google.common.io.Files.asByteSource(
                uploadedImage.getOriginalImagePath(appConfig).toFile())
            .hash(Hashing.sha256())
            .toString();
    uploadedImage.setContentHash(hash);
    return findAlbumElementByContentHash(hash, uploadedImage.getAlbumId()).isPresent();
  }

  public Optional<AlbumElement> findAlbumElementByContentHash(String hashStr, Long id) {
    Optional<AlbumElement> byContentHashAndAlbumId =
        albumElementRepository.findByContentHashAndAlbumId(hashStr, id);
    if (byContentHashAndAlbumId.isPresent()) {
      log.debug("Duplicate hash found(disk): {}", hashStr);
    }
    return byContentHashAndAlbumId;
  }
}

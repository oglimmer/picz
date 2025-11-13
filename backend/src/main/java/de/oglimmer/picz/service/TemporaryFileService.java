/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import de.oglimmer.picz.config.TemporaryFileStorageProps;
import de.oglimmer.picz.db.TemporaryFiles;
import de.oglimmer.picz.db.TemporaryFilesRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemporaryFileService {

  private final TemporaryFileStorageProps storageProps;
  private final TemporaryFilesRepository temporaryFilesRepository;
  private final RestTemplate restTemplate;

  public void save(String uniqueId, byte[] content) {
    try {
      saveToRestApi(uniqueId, content);
      log.debug("Successfully saved file {} to REST API", uniqueId);
    } catch (Exception e) {
      log.warn(
          "Failed to save file {} to REST API, falling back to database: {}",
          uniqueId,
          e.getMessage());
      saveToDatabase(uniqueId, content);
    }
  }

  public Optional<byte[]> findByUniqueId(String uniqueId) {
    try {
      byte[] content = loadFromRestApi(uniqueId);
      log.debug("Successfully loaded file {} from REST API", uniqueId);
      return Optional.of(content);
    } catch (Exception e) {
      log.warn(
          "Failed to load file {} from REST API, falling back to database: {}",
          uniqueId,
          e.getMessage());
      return loadFromDatabase(uniqueId);
    }
  }

  public void deleteByUniqueId(String uniqueId) {
    try {
      deleteFromRestApi(uniqueId);
      log.debug("Successfully deleted file {} from REST API", uniqueId);
    } catch (Exception e) {
      log.warn(
          "Failed to delete file {} from REST API, falling back to database: {}",
          uniqueId,
          e.getMessage());
    }

    // Always try to delete from database as well in case it was stored there
    deleteFromDatabase(uniqueId);
  }

  private void saveToRestApi(String uniqueId, byte[] content) {
    String url = storageProps.getBaseUrl() + "/" + storageProps.getBucket() + "/" + uniqueId;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

    HttpEntity<byte[]> entity = new HttpEntity<>(content, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("REST API returned status: " + response.getStatusCode());
    }
  }

  private byte[] loadFromRestApi(String uniqueId) {
    String url = storageProps.getBaseUrl() + "/" + storageProps.getBucket() + "/" + uniqueId;

    ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("REST API returned status: " + response.getStatusCode());
    }

    return response.getBody();
  }

  private void deleteFromRestApi(String uniqueId) {
    String url = storageProps.getBaseUrl() + "/" + storageProps.getBucket() + "/" + uniqueId;

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("REST API returned status: " + response.getStatusCode());
    }
  }

  private void saveToDatabase(String uniqueId, byte[] content) {
    TemporaryFiles temporaryFiles =
        TemporaryFiles.builder().uniqueId(uniqueId).content(content).build();
    temporaryFilesRepository.save(temporaryFiles);
  }

  private Optional<byte[]> loadFromDatabase(String uniqueId) {
    return temporaryFilesRepository.findByUniqueId(uniqueId).map(TemporaryFiles::getContent);
  }

  private void deleteFromDatabase(String uniqueId) {
    temporaryFilesRepository.deleteByUniqueId(uniqueId);
  }
}

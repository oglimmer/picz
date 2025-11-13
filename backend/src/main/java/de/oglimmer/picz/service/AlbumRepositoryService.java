/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.*;
import de.oglimmer.picz.util.DateCompare;
import de.oglimmer.picz.util.RandomData;
import de.oglimmer.picz.util.UserContextHolder;
import de.oglimmer.picz.web.dto.PatchAlbumRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class AlbumRepositoryService {

  private static final boolean MAP_APPLE = false; // Set to false to use Google Maps

  private final AlbumElementRepository albumElementRepository;
  private final AlbumRepository albumRepository;
  private final UserRepository userRepository;
  private final AppConfig appConfig;
  private final AppleMapGenerator mapAppleGenerator;
  private final GoogleMapGenerator mapGoogleGenerator;
  private final ImageResizeService imageResizeService;
  private final AlbumElementFileDeletionService albumElementFileDeletionService;
  private final DuplicatedHashService duplicatedHashService;
  private final CapacityService capacityService;
  private final ImageStorage imageStorage;

  public Album get(long id) {
    Optional<Album> albumOptional = albumRepository.findById(id);
    if (albumOptional.isEmpty()) {
      throw new IllegalArgumentException("Album not found");
    }
    Album album = albumOptional.get();
    User user = UserContextHolder.getUser();
    if (!user.isAdmin() && !album.getUser().equals(user)) {
      throw new RuntimeException("Access denied");
    }
    return album;
  }

  public Iterable<Album> findAll() {
    return albumRepository.findAll();
  }

  public void createSection(long albumId, long imageId) {
    AlbumElement image = albumElementRepository.findById(imageId).orElseThrow();
    if (!image.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    if (image.getAlbum().getId() != albumId) {
      throw new IllegalArgumentException("Image is not part of album");
    }
    createDateElement(image);
  }

  public void createMap(long albumId, Long imageId) {
    AlbumElement image = albumElementRepository.findById(imageId).orElseThrow();
    if (!image.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    if (image.getAlbum().getId() != albumId) {
      throw new IllegalArgumentException("Image is not part of album");
    }
    createMapElement(image);
  }

  public AlbumElement updateMap(
      long albumId,
      Long imageId,
      Double mapCenterLongitude,
      Double mapCenterLatitude,
      Double mapSpanLongitude,
      Double mapSpanLatitude,
      Double longitude,
      Double latitude) {
    AlbumElement image = albumElementRepository.findById(imageId).orElseThrow();
    if (!"apple".equals(image.getMapType())) {
      throw new RuntimeException("Wrong type of map element: " + image.getMapType());
    }
    if (!image.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    if (image.getAlbum().getId() != albumId) {
      throw new IllegalArgumentException("Image is not part of album");
    }
    updateMapElement(
        image,
        mapCenterLongitude,
        mapCenterLatitude,
        mapSpanLongitude,
        mapSpanLatitude,
        longitude,
        latitude);
    return image;
  }

  public AlbumElement updateGoogleMap(
      long albumId,
      Long imageId,
      Double mapCenterLatitude,
      Double mapCenterLongitude,
      Double markerLatitude,
      Double markerLongitude,
      Integer zoomLevel) {
    AlbumElement image = albumElementRepository.findById(imageId).orElseThrow();
    if (!"google".equals(image.getMapType())) {
      throw new RuntimeException("Wrong type of map element: " + image.getMapType());
    }
    if (!image.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    if (image.getAlbum().getId() != albumId) {
      throw new IllegalArgumentException("Image is not part of album");
    }
    updateMapElement(
        image, mapCenterLatitude, mapCenterLongitude, markerLatitude, markerLongitude, zoomLevel);
    return image;
  }

  @SneakyThrows
  private void createMapElement(AlbumElement currentElement) {
    Album album = currentElement.getAlbum();
    List<AlbumElement> albumElements = album.getAlbumElements();

    albumElements.sort(Comparator.comparingLong(AlbumElement::getOrderNo));

    int currentIndex = albumElements.indexOf(currentElement);
    int previousIndex = currentIndex - 1;

    long newOrderNo;
    if (previousIndex >= 0 && previousIndex < albumElements.size()) {
      AlbumElement previousElement = albumElements.get(previousIndex);
      newOrderNo =
          (currentElement.getOrderNo() - previousElement.getOrderNo()) / 2
              + previousElement.getOrderNo();
    } else {
      newOrderNo = currentElement.getOrderNo() - 1000;
    }

    String filename = RandomData.generateRandomString(32) + ".png";
    Path imagePath = Paths.get(appConfig.getImagePath() + filename);
    double longitude;
    double latitude;
    if (currentElement.getLongitude() != null && currentElement.getLatitude() != null) {
      longitude = currentElement.getLongitude();
      latitude = currentElement.getLatitude();
    } else {
      Optional<AlbumElement> first =
          album.getAlbumElements().stream()
              .filter(e -> e.getLongitude() != null && e.getLatitude() != null)
              .findFirst();
      if (first.isPresent()) {
        AlbumElement albumElement = first.get();
        longitude = albumElement.getLongitude();
        latitude = albumElement.getLatitude();
      } else {
        longitude = -122.00893703585939; // apple campus
        latitude = 37.33490385701012;
      }
    }

    byte[] image;
    if (MAP_APPLE) {
      log.debug("Using Apple Maps for map generation");
      image = mapAppleGenerator.generateMap(latitude, longitude, latitude, longitude, 0.5, 0.5);
    } else {
      log.debug("Using Google Maps for map generation");
      image = mapGoogleGenerator.generateMap(latitude, longitude, latitude, longitude, 10);
    }
    Files.write(imagePath, image);

    AlbumElement dateElement = new AlbumElement();
    dateElement.setSecretId(RandomData.generateRandomString(32));
    dateElement.setElementType(AlbumElementType.MAP);
    dateElement.setOrderNo(newOrderNo);
    dateElement.setAlbum(album);
    dateElement.setFilename(filename);
    dateElement.setLatitude(latitude);
    dateElement.setLongitude(longitude);
    dateElement.setMapCenterLatitude(latitude);
    dateElement.setMapCenterLongitude(longitude);
    dateElement.setMapSpanLatitude(0.5);
    dateElement.setMapSpanLongitude(0.5);
    dateElement.setZoomLevel(10);
    dateElement.setMapType(MAP_APPLE ? "apple" : "google");
    albumElementRepository.save(dateElement);

    Path smallPath = Paths.get(appConfig.getSmallImagePath() + filename);
    imageResizeService.resize(
        imagePath,
        smallPath,
        ImageCreateService.PREVIEW_IMAGE_WIDTH,
        album.getUser().getJpgQuality());

    // check available space
    long fileSize = imagePath.toFile().length();
    long smallFileSize = smallPath.toFile().length();

    if (capacityService.useCapacity(album.getUser().getId(), fileSize + smallFileSize)) {
      imagePath.toFile().delete();
      smallPath.toFile().delete();
    } else {
      imageStorage.transferToPersistentStorage(imagePath, smallPath);
    }
  }

  @SneakyThrows
  private void updateMapElement(
      AlbumElement dateElement,
      Double mapCenterLongitude,
      Double mapCenterLatitude,
      Double mapSpanLongitude,
      Double mapSpanLatitude,
      Double longitude,
      Double latitude) {
    albumElementFileDeletionService.deleteFromPersistentStorage(dateElement);
    Album album = dateElement.getAlbum();
    String filename = RandomData.generateRandomString(32) + ".png";
    Path imagePath = Paths.get(appConfig.getImagePath() + filename);
    byte[] image =
        mapAppleGenerator.generateMap(
            latitude,
            longitude,
            mapCenterLatitude,
            mapCenterLongitude,
            mapSpanLatitude,
            mapSpanLongitude);
    Files.write(imagePath, image);

    dateElement.setSecretId(RandomData.generateRandomString(32));
    dateElement.setFilename(filename);
    dateElement.setLongitude(longitude);
    dateElement.setLatitude(latitude);
    dateElement.setMapCenterLatitude(mapCenterLatitude);
    dateElement.setMapCenterLongitude(mapCenterLongitude);
    dateElement.setMapSpanLatitude(mapSpanLatitude);
    dateElement.setMapSpanLongitude(mapSpanLongitude);

    Path smallPath = Paths.get(appConfig.getSmallImagePath() + filename);
    imageResizeService.resize(
        imagePath,
        smallPath,
        ImageCreateService.PREVIEW_IMAGE_WIDTH,
        album.getUser().getJpgQuality());

    imageStorage.transferToPersistentStorage(imagePath, smallPath);
  }

  public void createDateElement(AlbumElement currentElement) {
    Album album = currentElement.getAlbum();
    List<AlbumElement> albumElements = album.getAlbumElements();

    albumElements.sort(Comparator.comparingLong(AlbumElement::getOrderNo));

    int currentIndex = albumElements.indexOf(currentElement);
    int previousIndex = currentIndex - 1;

    long newOrderNo;
    if (previousIndex >= 0 && previousIndex < albumElements.size()) {
      AlbumElement previousElement = albumElements.get(previousIndex);
      newOrderNo =
          (currentElement.getOrderNo() - previousElement.getOrderNo()) / 2
              + previousElement.getOrderNo();
    } else {
      newOrderNo = currentElement.getOrderNo() - 1000;
    }

    AlbumElement albumElement = getAlbumElement(album, newOrderNo);
    albumElement.setDescription("New section");
  }

  @SneakyThrows
  private void updateMapElement(
      AlbumElement dateElement,
      Double mapCenterLatitude,
      Double mapCenterLongitude,
      Double markerLatitude,
      Double markerLongitude,
      Integer zoomLevel) {
    albumElementFileDeletionService.deleteFromPersistentStorage(dateElement);
    Album album = dateElement.getAlbum();
    String filename = RandomData.generateRandomString(32) + ".png";
    Path imagePath = Paths.get(appConfig.getImagePath() + filename);
    byte[] image =
        mapGoogleGenerator.generateMap(
            mapCenterLatitude, mapCenterLongitude, markerLatitude, markerLongitude, zoomLevel);
    Files.write(imagePath, image);

    dateElement.setSecretId(RandomData.generateRandomString(32));
    dateElement.setFilename(filename);
    dateElement.setMapCenterLatitude(mapCenterLatitude);
    dateElement.setMapCenterLongitude(mapCenterLongitude);
    dateElement.setLatitude(markerLatitude);
    dateElement.setLongitude(markerLongitude);
    dateElement.setZoomLevel(zoomLevel);

    Path smallPath = Paths.get(appConfig.getSmallImagePath() + filename);
    imageResizeService.resize(
        imagePath,
        smallPath,
        ImageCreateService.PREVIEW_IMAGE_WIDTH,
        album.getUser().getJpgQuality());

    imageStorage.transferToPersistentStorage(imagePath, smallPath);
  }

  public AlbumElement createDateElementForDay(AlbumElement currentElement) {
    Album album = currentElement.getAlbum();

    Date date = currentElement.getCreationDate();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);

    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    Date midnightDate = calendar.getTime();
    long orderNo = midnightDate.getTime();

    return getAlbumElement(album, orderNo);
  }

  private AlbumElement getAlbumElement(Album album, long orderNo) {
    AlbumElement dateElement = new AlbumElement();
    dateElement.setSecretId(RandomData.generateRandomString(32));
    dateElement.setElementType(AlbumElementType.SECTION);
    dateElement.setOrderNo(orderNo);
    dateElement.setAlbum(album);
    albumElementRepository.save(dateElement);
    log.debug(
        "created date element {} for album:{} with orderNo {}",
        dateElement.getId(),
        album.getId(),
        orderNo);
    return dateElement;
  }

  public void deleteElement(long id, long elementId) {
    Optional<AlbumElement> albumElement = albumElementRepository.findById(elementId);
    if (albumElement.isEmpty()) {
      return;
    }
    AlbumElement ae = albumElement.get();
    if (ae.getAlbum().getId() != id) {
      throw new RuntimeException("element does not belong to album");
    }
    if (!ae.getAlbum().getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    if (ae.getElementType() == AlbumElementType.IMAGE
        || ae.getElementType() == AlbumElementType.MAP) {
      albumElementFileDeletionService.deleteFromPersistentStorage(ae);
    }
    albumElementRepository.delete(ae);
  }

  public void reorder(long id, int oldOrder, int newOrder) {
    Album album = albumRepository.findById(id).orElseThrow();
    if (!album.getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    List<AlbumElement> albumElements = album.getAlbumElements();
    albumElements.sort(Comparator.comparingLong(AlbumElement::getOrderNo));
    AlbumElement source = albumElements.get(oldOrder);
    AlbumElement target = albumElements.get(newOrder);
    long newOrderNo;
    if (newOrder < oldOrder) {
      newOrderNo =
          newOrder > 0
              ? (target.getOrderNo() + albumElements.get(newOrder - 1).getOrderNo()) / 2
              : target.getOrderNo() - 1000;
    } else {
      newOrderNo =
          newOrder < albumElements.size() - 1
              ? (albumElements.get(newOrder + 1).getOrderNo() + target.getOrderNo()) / 2
              : target.getOrderNo() + 1000;
    }
    source.setOrderNo(newOrderNo);
  }

  public Album findBySecretId(String secretId) {
    return albumRepository
        .findBySecretId(secretId)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Album " + secretId + " not found"));
  }

  public AlbumElement findAlbumElementById(long id) {
    return albumElementRepository.findById(id).orElseThrow();
  }

  public AlbumElement findAlbumElementBySecretId(String id) {
    log.debug("findAlbumElementBySecretId {}", id);
    return albumElementRepository
        .findBySecretId(id)
        .orElseThrow(() -> new RuntimeException("AlbumElement " + id + " not found"));
  }

  public Optional<AlbumElement> findAlbumElementByFilename(String filename) {
    return albumElementRepository.findByFilename(filename);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @SuppressWarnings("unused") // used by job enqueuer
  public void createSectionIfNeeded(Long imageElementId) {
    AlbumElement imageEntity =
        albumElementRepository
            .findById(imageElementId)
            .orElseThrow(
                () -> new RuntimeException("ImageEntity not found for id: " + imageElementId));

    log.debug("createDateSection start for id={}", imageEntity.getId());
    if (!existsSectionForDay(imageEntity)) {
      log.debug("createDateSection will create section for day for id={}", imageEntity.getId());
      AlbumElement newDateElement = createDateElementForDay(imageEntity);
      newDateElement.setDescription(dateToString(imageEntity.getCreationDate()));
    }
  }

  private String dateToString(Date creationDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
    return dateFormat.format(creationDate);
  }

  private boolean existsSectionForDay(AlbumElement imageEntity) {
    return imageEntity.getAlbum().getAlbumElements().stream()
        .filter(e -> e.getElementType() == AlbumElementType.SECTION)
        .anyMatch(e -> 0 == DateCompare.compareByDay(e.getOrderNo(), imageEntity.getOrderNo()));
  }

  public Optional<Album> findById(long albumId) {
    return albumRepository.findById(albumId);
  }

  public AlbumElement createAlbumElement(AlbumElement imageEntity) {
    return albumElementRepository.save(imageEntity);
  }

  public Iterable<Album> findByUserTokenSubject(String subject) {
    return albumRepository.findByUserTokenSubject(subject);
  }

  public Album create() {
    String tokenSubject = UserContextHolder.getUser().getTokenSubject();
    Optional<User> userOpt = userRepository.findByTokenSubject(tokenSubject);
    User user = userOpt.orElseThrow();
    Album album = new Album();
    album.setSecretId(RandomData.generateRandomString(32));
    album.setDescription("New Album");
    album.setUser(user);
    albumRepository.save(album);
    return album;
  }

  public void patchAlbum(long id, PatchAlbumRequest patchAlbumRequest) {
    Optional<Album> byId = albumRepository.findById(id);
    if (byId.isPresent()) {
      Album album = byId.get();
      if (!album.getUser().equals(UserContextHolder.getUser())) {
        throw new RuntimeException("Access denied");
      }
      album.setDescription(patchAlbumRequest.getDescription());
    }
  }

  public void deleteAlbum(long id) {
    Album album = albumRepository.findById(id).orElseThrow();
    if (!album.getUser().equals(UserContextHolder.getUser())) {
      throw new RuntimeException("Access denied");
    }
    album.getAlbumElements().forEach(albumElementFileDeletionService::deleteFromPersistentStorage);
    albumElementRepository.deleteAllById(
        album.getAlbumElements().stream().mapToLong(AlbumElement::getId).boxed().toList());
    albumRepository.deleteById(id);
  }
}

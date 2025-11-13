/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web;

import de.oglimmer.picz.db.Album;
import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.db.AlbumElementType;
import de.oglimmer.picz.db.User;
import de.oglimmer.picz.service.AlbumRepositoryService;
import de.oglimmer.picz.util.UserContextHolder;
import de.oglimmer.picz.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/album")
@AllArgsConstructor
public class AlbumController {

  private AlbumRepositoryService albumRepositoryService;
  private ModelMapper modelMapper;

  @Operation(
      summary = "Creates a new empty album",
      security = @SecurityRequirement(name = "OpenID"))
  @PostMapping()
  public CreateAlbumResponse create() {
    return modelMapper.map(albumRepositoryService.create(), CreateAlbumResponse.class);
  }

  @Operation(
      summary = "Changing the attributes of the album. Currently supported description",
      security = @SecurityRequirement(name = "OpenID"))
  @PatchMapping("/{id}")
  public void patchAlbum(
      @RequestBody PatchAlbumRequest patchAlbumRequest, @PathVariable("id") long id) {
    albumRepositoryService.patchAlbum(id, patchAlbumRequest);
  }

  @Operation(
      summary = "Creates a new section, which can provide a title image",
      security = @SecurityRequirement(name = "OpenID"))
  @PostMapping(value = "/{id}/section")
  public void createSection(
      @RequestBody CreateAlbumSectionRequest createAlbumSectionRequest,
      @PathVariable("id") long id) {
    albumRepositoryService.createSection(id, createAlbumSectionRequest.getImageId());
  }

  @Operation(
      summary = "Creates a new map (using Apple or Google depending on the configuration)",
      security = @SecurityRequirement(name = "OpenID"))
  @PostMapping(value = "/{id}/map")
  public void createMap(
      @RequestBody CreateAlbumMapRequest createAlbumMapRequest, @PathVariable("id") long id) {
    albumRepositoryService.createMap(id, createAlbumMapRequest.getImageId());
  }

  @Operation(
      summary = "Allows to update a map entry when using the Apple Maps SDK",
      security = @SecurityRequirement(name = "OpenID"))
  @PutMapping(value = "/{id}/map")
  public AlbumElementResponse updateMap(
      @RequestBody UpdateAlbumMapRequest updateAlbumMapRequest, @PathVariable("id") long id) {
    AlbumElement albumElement =
        albumRepositoryService.updateMap(
            id,
            updateAlbumMapRequest.getImageId(),
            updateAlbumMapRequest.getMapCenterLongitude(),
            updateAlbumMapRequest.getMapCenterLatitude(),
            updateAlbumMapRequest.getMapSpanLongitude(),
            updateAlbumMapRequest.getMapSpanLatitude(),
            updateAlbumMapRequest.getLongitude(),
            updateAlbumMapRequest.getLatitude());
    return modelMapper.map(albumElement, AlbumElementResponse.class);
  }

  @Operation(
      summary = "Allows to update a map entry when using the google sdk",
      security = @SecurityRequirement(name = "OpenID"))
  @PutMapping(value = "/{id}/map-google")
  public AlbumElementResponse updateMapGoogle(
      @RequestBody UpdateAlbumGoogleMapRequest updateAlbumMapRequest, @PathVariable("id") long id) {
    AlbumElement albumElement =
        albumRepositoryService.updateGoogleMap(
            id,
            updateAlbumMapRequest.getImageId(),
            updateAlbumMapRequest.getMapCenterLatitude(),
            updateAlbumMapRequest.getMapCenterLongitude(),
            updateAlbumMapRequest.getMarkerLatitude(),
            updateAlbumMapRequest.getMarkerLongitude(),
            updateAlbumMapRequest.getZoomLevel());
    return modelMapper.map(albumElement, AlbumElementResponse.class);
  }

  @Operation(
      summary = "Changes the order of images inside an album",
      security = @SecurityRequirement(name = "OpenID"))
  @PostMapping(value = "/{id}/order")
  public void reorder(
      @RequestBody OrderAlbumRequest orderAlbumRequest, @PathVariable("id") long id) {
    albumRepositoryService.reorder(
        id, orderAlbumRequest.getOldIndex(), orderAlbumRequest.getNewIndex());
  }

  @Operation(
      summary = "Deletes an element inside an album",
      security = @SecurityRequirement(name = "OpenID"))
  @DeleteMapping(value = "/{id}/element/{elementId}")
  public void deleteElement(
      @PathVariable("id") long id, @PathVariable("elementId") long elementId) {
    albumRepositoryService.deleteElement(id, elementId);
  }

  @Operation(summary = "Deletes an entire album", security = @SecurityRequirement(name = "OpenID"))
  @DeleteMapping(value = "/{id}")
  public void deleteAlbum(@PathVariable("id") long id) {
    albumRepositoryService.deleteAlbum(id);
  }

  @Operation(
      summary = "Returns an album with all associated entries, like images, titles and maps",
      security = @SecurityRequirement(name = "OpenID"))
  @GetMapping(value = "/{id}")
  public GetAlbumResponseImage get(@PathVariable("id") long id) {
    Album album = albumRepositoryService.get(id);
    GetAlbumResponseImage response = modelMapper.map(album, GetAlbumResponseImage.class);
    response.getAlbumElements().sort(Comparator.comparing(AlbumElementResponse::getOrderNo));
    return response;
  }

  @Operation(
      summary = "Returns a albums for an user",
      security = @SecurityRequirement(name = "OpenID"))
  @GetMapping
  public ResponseEntity<List<ListAlbumResponse>> list() {
    User user = UserContextHolder.getUser();
    Iterable<Album> all;
    if (user.isAdmin()) {
      all = albumRepositoryService.findAll();
    } else {
      all = albumRepositoryService.findByUserTokenSubject(user.getTokenSubject());
    }
    List<Album> allList = StreamSupport.stream(all.spliterator(), false).toList();
    List<ListAlbumResponse> list =
        allList.stream()
            .map(album -> modelMapper.map(album, ListAlbumResponse.class))
            .collect(Collectors.toList());
    list.forEach(
        e ->
            e.setOwner(
                StreamSupport.stream(all.spliterator(), false)
                    .filter(f -> Objects.equals(f.getId(), e.getId()))
                    .findAny()
                    .orElseThrow()
                    .getUser()
                    .getEmail()));
    if (user.isAdmin()) {
      list.forEach(e -> e.setDescription(e.getDescription() + " (" + e.getOwner() + ")"));
    }
    list.forEach(
        e ->
            e.setTitleSecretId(
                allList.stream()
                    .filter(f -> f.getId().equals(e.getId()))
                    .findAny()
                    .orElseThrow()
                    .getAlbumElements()
                    .stream()
                    .filter(g -> g.getElementType() == AlbumElementType.IMAGE)
                    .findAny()
                    .map(AlbumElement::getSecretId)
                    .orElse(null)));
    list.forEach(
        e ->
            e.setImageCount(
                allList.stream()
                    .filter(f -> f.getId().equals(e.getId()))
                    .findAny()
                    .orElseThrow()
                    .getAlbumElements()
                    .stream()
                    .filter(g -> g.getElementType() == AlbumElementType.IMAGE)
                    .count()));
    return ResponseEntity.ok(list);
  }
}

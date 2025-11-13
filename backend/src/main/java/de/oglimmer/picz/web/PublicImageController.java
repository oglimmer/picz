/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web;

import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.service.AlbumRepositoryService;
import de.oglimmer.picz.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/v1/image")
@AllArgsConstructor
public class PublicImageController {

  private ImageService imageService;
  private AlbumRepositoryService albumRepositoryService;

  private String createExpiresHeader() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_YEAR, 7);
    Date expiresDate = calendar.getTime();
    return dateFormat.format(expiresDate);
  }

  private String createLastModified(LocalDateTime localDateTime) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    ZoneId zoneId = ZoneId.of("GMT");
    Instant instant = localDateTime.atZone(zoneId).toInstant();
    Date date = Date.from(instant);
    return dateFormat.format(date);
  }

  @Operation(
      summary =
          "Returns the image for a given secret id. A query-parameter defines small or original size")
  @GetMapping(
      value = "/{id}",
      produces = {IMAGE_JPEG_VALUE})
  public ResponseEntity<byte[]> get(
      @PathVariable("id") String id,
      @RequestParam(value = "small", required = false, defaultValue = "false") boolean small) {
    AlbumElement albumElement = albumRepositoryService.findAlbumElementBySecretId(id);
    byte[] body = imageService.get(albumElement, small);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cache-Control", "public, max-age=604800");
    headers.set("Expires", createExpiresHeader());
    headers.set("ETag", albumElement.getContentHash());
    headers.set("Last-Modified", createLastModified(albumElement.getEntryLastUpdateDate()));
    return ResponseEntity.ok().headers(headers).body(body);
  }
}

/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web.dto;

import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ImageUploadStats {
  private Set<Long> processingAlbumIds;
  private Map<Long, Integer> processingCounterByAlbum;
  private Map<Long, Integer> numberOfImagesByAlbum;
}

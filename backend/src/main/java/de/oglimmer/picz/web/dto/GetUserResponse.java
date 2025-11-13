/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web.dto;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserResponse {

  private boolean acceptedTos;
  private String email;
  private long usedCapacity;
  private long capacity;
  private Set<Long> processingAlbumIds;
  private Map<Long, Integer> processingCounterByAlbum;
  private Map<Long, Integer> numberOfImagesByAlbum;
}

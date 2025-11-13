/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
public class GetAlbumResponseImage {

  private Long id;

  private List<AlbumElementResponse> albumElements;

  private String description;

  private String secretId;
}

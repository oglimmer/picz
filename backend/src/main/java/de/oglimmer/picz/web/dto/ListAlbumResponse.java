/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.web.dto;

import lombok.Data;

@Data
public class ListAlbumResponse {

  private Long id;

  private String description;

  private Long imageCount;

  private String secretId;

  private String owner;

  private String titleSecretId;
}

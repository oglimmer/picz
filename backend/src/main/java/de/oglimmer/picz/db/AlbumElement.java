/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.db;

import com.drew.lang.annotations.NotNull;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_album_element_hash",
          columnNames = {"album_id", "contentHash"})
    })
public class AlbumElement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "album_id")
  private Album album;

  @Column(nullable = false)
  @NotNull
  @Enumerated(EnumType.STRING)
  private AlbumElementType elementType;

  // data for all
  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  @NotNull
  private Long orderNo;

  // data for image
  @Column(nullable = false)
  @NotNull
  private String secretId;

  private String filename;

  private String contentHash;

  private Date creationDate;

  private Double longitude;

  private Double latitude;

  private Double mapCenterLongitude;
  private Double mapCenterLatitude;
  private Double mapSpanLongitude;
  private Double mapSpanLatitude;
  private Integer zoomLevel;
  private String mapType;

  @Column(updatable = false, insertable = false)
  private LocalDateTime entryCreationDate;

  @Column(updatable = false, insertable = false)
  private LocalDateTime entryLastUpdateDate;

  public long getSequencingId() {
    return album.getId();
  }
}

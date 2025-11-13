/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.db;

import com.drew.lang.annotations.NotNull;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = {"albumId", "userId"})
@Table(
    name = "queue_count_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"album_id", "user_id"}))
public class QueueCountStat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "album_id", nullable = false)
  @NotNull
  private Long albumId;

  @Column(name = "user_id", nullable = false)
  @NotNull
  private Long userId;

  @Column(name = "processing_count", nullable = false)
  @NotNull
  private Integer processingCount = 0;
}

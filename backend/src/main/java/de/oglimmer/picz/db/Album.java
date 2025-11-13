/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.db;

import com.drew.lang.annotations.NotNull;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = "id")
public class Album {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "album")
  private List<AlbumElement> albumElements;

  private String description;

  @Column(nullable = false)
  @NotNull
  private String secretId;

  @Column(updatable = false, insertable = false)
  private LocalDateTime creationDate;

  @Column(updatable = false, insertable = false)
  private LocalDateTime lastUpdateDate;
}

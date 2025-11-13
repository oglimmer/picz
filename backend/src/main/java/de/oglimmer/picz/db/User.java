/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.db;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class User {

  public static final User ANONYMOUS = new User("anonymous", "anonymous", "none");

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;

  @Column(unique = true)
  private String tokenSubject;

  private String issuer;

  private Long capacity;

  private Long usedCapacity;

  private boolean acceptedTos;

  private float jpgQuality;

  private int maxImageWidth;

  private boolean admin;

  @Column(updatable = false, insertable = false)
  private LocalDateTime creationDate;

  @Column(updatable = false, insertable = false)
  private LocalDateTime lastUpdateDate;

  public User(String email, String tokenSubject, String issuer) {
    this.email = email;
    this.tokenSubject = tokenSubject;
    this.issuer = issuer;
    this.capacity = 1024L * 1024L * 50L; // 50 MB
    this.usedCapacity = 0L;
    this.jpgQuality = 0.4f;
    this.maxImageWidth = 1200;
  }
}

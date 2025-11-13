/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.db;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface AlbumElementRepository extends ListCrudRepository<AlbumElement, Long> {

  Optional<AlbumElement> findBySecretId(String secretId);

  Optional<AlbumElement> findByFilename(String filename);

  Optional<AlbumElement> findByContentHashAndAlbumId(String hashStr, Long id);
}

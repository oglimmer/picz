package de.oglimmer.picz.db;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface AlbumElementRepository extends ListCrudRepository<AlbumElement, Long> {

    Optional<AlbumElement> findBySecretId(String secretId);

    Optional<AlbumElement> findByFilename(String filename);

    Optional<AlbumElement> findByContentHashAndAlbumId(String hashStr, Long id);
}

/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.db;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface TemporaryFilesRepository extends ListCrudRepository<TemporaryFiles, Long> {

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  TemporaryFiles save(TemporaryFiles entity);

  Optional<TemporaryFiles> findByUniqueId(String uniqueId);

  void deleteByUniqueId(String uniqueId);
}

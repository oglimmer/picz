package de.oglimmer.picz.db;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface TemporaryFilesRepository extends ListCrudRepository<TemporaryFiles, Long> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TemporaryFiles save(TemporaryFiles entity);

    Optional<TemporaryFiles> findByUniqueId(String uniqueId);

    void deleteByUniqueId(String uniqueId);
}

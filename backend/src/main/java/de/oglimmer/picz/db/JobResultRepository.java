package de.oglimmer.picz.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobResultRepository extends JpaRepository<JobResult, Long> {
}
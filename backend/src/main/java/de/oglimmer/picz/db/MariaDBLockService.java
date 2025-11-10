package de.oglimmer.picz.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <a href="https://mariadb.com/kb/en/get_lock/">documentation for get_lock</a>
 */
@Slf4j
@Service
public class MariaDBLockService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void doWithLock(String lockName, Runnable task) {
        Integer result = (Integer) em
                .createNativeQuery("SELECT GET_LOCK(:lockName, 0)")
                .setParameter("lockName", lockName)
                .getSingleResult();

        if (result == null || result != 1) {
            // this will spam in multi-host setups, so we only log it in debug mode
//            log.warn("Could not acquire lock {}", lockName);
            return;
        }

        try {
            task.run();
        } finally {
            Integer release = (Integer) em
                    .createNativeQuery("SELECT RELEASE_LOCK(:lockName)")
                    .setParameter("lockName", lockName)
                    .getSingleResult();

            if (release == null || release != 1) {
                log.warn("Failed to release lock {}", lockName);
            }
        }
    }
}
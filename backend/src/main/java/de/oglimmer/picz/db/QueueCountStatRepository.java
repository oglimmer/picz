package de.oglimmer.picz.db;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface QueueCountStatRepository extends CrudRepository<QueueCountStat, Long> {

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO queue_count_stats (album_id, user_id, processing_count)" +
                    "     VALUES (:albumId, :userId, 1)" +
                    "  ON DUPLICATE KEY UPDATE processing_count = processing_count + 1",
            nativeQuery = true
    )
    void upsertIncrementProcessingCount(
            @Param("albumId") long albumId,
            @Param("userId") long userId
    );

    /**
     * Decrement processing_count by 1, but only if processing_count > 1.
     * <p>
     * Returns the number of rows actually updated (0 or 1).
     */
    @Modifying
    @Query(
            "UPDATE QueueCountStat q" +
                    "   SET q.processingCount = q.processingCount - 1" +
                    " WHERE q.albumId = :albumId" +
                    "   AND q.userId  = :userId" +
                    "   AND q.processingCount > 1"
    )
    int decrementIfGreaterThanOne(
            @Param("albumId") long albumId,
            @Param("userId") long userId
    );

    /**
     * Delete the row, but only if processing_count == 1.
     * <p>
     * Returns the number of rows actually deleted (0 or 1).
     */
    @Modifying
    @Query(
            "DELETE FROM QueueCountStat q" +
                    " WHERE q.albumId = :albumId" +
                    "   AND q.userId  = :userId" +
                    "   AND q.processingCount = 1"
    )
    int deleteIfCountEqualsOne(
            @Param("albumId") long albumId,
            @Param("userId") long userId
    );

    List<QueueCountStat> findByAlbumIdIn(Set<Long> albumIds);

    @Query("SELECT DISTINCT q.albumId FROM QueueCountStat q WHERE q.userId = :userId")
    Set<Long> findAlbumIdsByUserId(Long userId);
}
package de.oglimmer.picz.service;

import de.oglimmer.picz.db.QueueCountStat;
import de.oglimmer.picz.db.QueueCountStatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRES_NEW)
public class QueueCountStats {

    private final QueueCountStatRepository queueCountStatRepository;

    public void incrementProcessingCounter(long albumId, long userId) {
        queueCountStatRepository.upsertIncrementProcessingCount(albumId, userId);
    }

    public void decrementProcessingCounter(long albumId, Long userId) {
        // 1) Try to decrement if count > 1
        int updated = queueCountStatRepository.decrementIfGreaterThanOne(albumId, userId);

        if (updated == 0) {
            // Either: (a) processing_count was exactly 1, or (b) the row didn’t exist.
            int deleted = queueCountStatRepository.deleteIfCountEqualsOne(albumId, userId);

            if (deleted == 0) {
                // No row with count == 1 (so there was never a row to begin with).
                log.warn("No album found for user {} with id {}", userId, albumId);
            }
        }
        // If updated == 1, we already decremented from >1 to >0. Done.
    }

    public Set<Long> getAlbumListInUploadByUser(long userId) {
        return queueCountStatRepository.findAlbumIdsByUserId(userId);
    }

    public Map<Long, Integer> getProcessingCounterByAlbum(Set<Long> albumIds) {
        return queueCountStatRepository.findByAlbumIdIn(albumIds).stream()
                .collect(Collectors.toMap(QueueCountStat::getAlbumId, QueueCountStat::getProcessingCount));
    }

}

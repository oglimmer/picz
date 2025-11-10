package de.oglimmer.picz.service;

import de.oglimmer.picz.db.User;
import de.oglimmer.picz.db.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;


@AllArgsConstructor
@Service
@Slf4j
public class CapacityService {

    private final UserRepository userRepository;

    @Transactional(REQUIRES_NEW)
    public boolean useCapacity(long userId, long capacityChange) {
        boolean result = true;
        User user = userRepository.findByIdLock(userId).orElseThrow();
        log.debug("Changing capacity for {}", capacityChange);
        if (user.getUsedCapacity() + capacityChange <= user.getCapacity()) {
            user.setUsedCapacity(user.getUsedCapacity() + capacityChange);
            result = false;
        }
        log.debug("changed capacity for user={} for {} now is {}", userId, capacityChange, user.getUsedCapacity());
        return result;
    }

}

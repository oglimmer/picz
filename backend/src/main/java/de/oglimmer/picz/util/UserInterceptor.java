package de.oglimmer.picz.util;

import de.oglimmer.picz.db.User;
import de.oglimmer.picz.db.UserRepository;
import de.oglimmer.picz.service.UserRepositoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final UserRepositoryService userRepositoryService;
    private final Lock lock = new ReentrantLock();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
//        log.debug("URL requested " + request.getRequestURL().toString());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user;
        if (authentication.getCredentials() instanceof Jwt jwt) {
            String sub = authentication.getName();
            Optional<User> optionalUser = userRepository.findByTokenSubject(sub);
            user = optionalUser.orElseGet(() -> {
                lock.lock();
                try {
                    return userRepositoryService.save(jwt);
                } finally {
                    lock.unlock();
                }
            });
        } else {
            user = User.ANONYMOUS;
        }
        UserContextHolder.setUser(user);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        UserContextHolder.clearUser();
    }
}

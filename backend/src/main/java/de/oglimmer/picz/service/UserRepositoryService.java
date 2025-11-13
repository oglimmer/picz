/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import de.oglimmer.picz.db.User;
import de.oglimmer.picz.db.UserRepository;
import de.oglimmer.picz.util.UserContextHolder;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Transactional
public class UserRepositoryService {

  private final UserRepository userRepository;
  private final AlbumRepositoryService albumRepositoryService;

  public void deleteUser() {
    User user = UserContextHolder.getUser();
    albumRepositoryService
        .findByUserTokenSubject(user.getTokenSubject())
        .forEach(e -> albumRepositoryService.deleteAlbum(e.getId()));
    userRepository.deleteById(user.getId());
  }

  public Optional<User> getUser() {
    return userRepository.findByTokenSubject(UserContextHolder.getUser().getTokenSubject());
  }

  public void acceptTos() {
    User user = getUser().orElseThrow();
    user.setAcceptedTos(true);
    userRepository.save(user);
  }

  public User save(Jwt jwt) {
    Optional<User> userOptional = userRepository.findByTokenSubject(jwt.getSubject());
    if (userOptional.isPresent()) {
      return userOptional.get();
    }
    User user =
        new User(jwt.getClaimAsString("email"), jwt.getSubject(), jwt.getIssuer().toExternalForm());
    return userRepository.save(user);
  }
}

package de.oglimmer.picz.service;

import com.google.common.hash.Hashing;
import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.db.AlbumElementRepository;
import de.oglimmer.picz.web.dto.UploadedImage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Service
@Slf4j
public class DuplicatedHashService {

    private final AlbumElementRepository albumElementRepository;
    private final AppConfig appConfig;

    @SneakyThrows
    public boolean checkForDuplicatedHash(UploadedImage uploadedImage) {
        String hash = com.google.common.io.Files
                .asByteSource(uploadedImage.getOriginalImagePath(appConfig).toFile())
                .hash(Hashing.sha256())
                .toString();
        uploadedImage.setContentHash(hash);
        return findAlbumElementByContentHash(hash, uploadedImage.getAlbumId()).isPresent();
    }

    public Optional<AlbumElement> findAlbumElementByContentHash(String hashStr, Long id) {
        Optional<AlbumElement> byContentHashAndAlbumId = albumElementRepository.findByContentHashAndAlbumId(hashStr, id);
        if (byContentHashAndAlbumId.isPresent()) {
            log.debug("Duplicate hash found(disk): {}", hashStr);
        }
        return byContentHashAndAlbumId;
    }

}

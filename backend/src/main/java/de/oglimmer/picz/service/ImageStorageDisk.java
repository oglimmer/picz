package de.oglimmer.picz.service;

import de.oglimmer.picz.config.AppConfig;
import de.oglimmer.picz.db.User;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@AllArgsConstructor
@Service
@ConditionalOnProperty(
        value = "picz.imageStorageImplementation",
        havingValue = "ImageStorageDisk")
public class ImageStorageDisk implements ImageStorage {

    private final AppConfig appConfig;

    @Override
    public long deletePersistentImagePath(String filename) {
        File file = new File(appConfig.getImagePath(), filename);
        long length = file.length();
        file.delete();
        return length;
    }

    @Override
    public long deletePersistentSmallImagePath(String filename) {
        File file = new File(appConfig.getSmallImagePath(), filename);
        long length = file.length();
        file.delete();
        return length;
    }

    @Override
    public void transferToPersistentStorage(Path imagePath, Path smallPath) {
        // no-op
    }

    @Override
    public Path loadFromPersistent(String filename, boolean small, User user) {
        String basePath = small ? appConfig.getSmallImagePath() : appConfig.getImagePath();
        Path path = Paths.get(basePath + filename);
        if (!path.toFile().exists()) {
            return null;
        }
        return path;
    }

}

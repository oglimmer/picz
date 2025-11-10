package de.oglimmer.picz.service;

import de.oglimmer.picz.db.User;

import java.nio.file.Path;

public interface ImageStorage {

    long deletePersistentImagePath(String filename);

    long deletePersistentSmallImagePath(String filename);

    void transferToPersistentStorage(Path imagePath, Path smallPath);

    Path loadFromPersistent(String filename, boolean small, User user);
}

/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.db.AlbumElementType;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Service
@AllArgsConstructor
public class AlbumElementFileDeletionService {

  private final CapacityService capacityService;
  private final ImageStorage imageStorage;

  @Async
  public void deleteFromPersistentStorage(AlbumElement ae) {
    if (ae.getElementType() == AlbumElementType.IMAGE
        || ae.getElementType() == AlbumElementType.MAP) {

      long imageFileSize = imageStorage.deletePersistentImagePath(ae.getFilename());
      long smallImageFileSize = imageStorage.deletePersistentSmallImagePath(ae.getFilename());
      if (ae.getElementType() == AlbumElementType.IMAGE) {
        capacityService.useCapacity(
            ae.getAlbum().getUser().getId(), -imageFileSize - smallImageFileSize);
      }
    }
  }
}

/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.picz.service;

import de.oglimmer.picz.db.AlbumElement;
import de.oglimmer.picz.util.queue.GenericJobEnqueuer;
import de.oglimmer.picz.web.dto.UploadedImage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@SuppressWarnings("unused") // This method is used by the job enqueuer
public class ImageCreationEntryService {

  private final GenericJobEnqueuer jobEnqueuer;
  private final UploadedImageToAlbumService uploadedImageToAlbumService;

  @SuppressWarnings("unused") // This method is used by the job enqueuer
  public void createImageEntry(UploadedImage uploadedImage) {

    // this runs on this host
    AlbumElement image = uploadedImageToAlbumService.createImage(uploadedImage);

    // this runs on any host, exactly one once
    jobEnqueuer.enqueueImmediately(
        "albumRepositoryService",
        "createSectionIfNeeded",
        new String[] {"java.lang.Long"},
        new Object[] {image.getId()},
        "MAIN_SCHEDULER");
  }
}

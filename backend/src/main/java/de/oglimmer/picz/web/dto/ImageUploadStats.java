package de.oglimmer.picz.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Builder
@Data
public class ImageUploadStats {
    private Set<Long> processingAlbumIds;
    private Map<Long, Integer> processingCounterByAlbum;
    private Map<Long, Integer> numberOfImagesByAlbum;
}

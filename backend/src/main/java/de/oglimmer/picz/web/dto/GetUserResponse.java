package de.oglimmer.picz.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class GetUserResponse {

    private boolean acceptedTos;
    private String email;
    private long usedCapacity;
    private long capacity;
    private Set<Long> processingAlbumIds;
    private Map<Long, Integer> processingCounterByAlbum;
    private Map<Long, Integer> numberOfImagesByAlbum;

}

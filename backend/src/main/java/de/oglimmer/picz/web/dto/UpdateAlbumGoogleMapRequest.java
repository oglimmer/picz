package de.oglimmer.picz.web.dto;

import lombok.Data;

@Data
public class UpdateAlbumGoogleMapRequest {
    private Long imageId;
    private Double markerLatitude;
    private Double markerLongitude;
    private Double mapCenterLatitude;
    private Double mapCenterLongitude;
    private Integer zoomLevel;
}

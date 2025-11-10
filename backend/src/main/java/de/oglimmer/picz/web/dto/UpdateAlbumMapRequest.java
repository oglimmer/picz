package de.oglimmer.picz.web.dto;

import lombok.Data;

@Data
public class UpdateAlbumMapRequest {
    private Long imageId;
    private Double longitude; // marker position
    private Double latitude; // marker position
    private Double mapCenterLongitude;
    private Double mapCenterLatitude;
    private Double mapSpanLongitude;
    private Double mapSpanLatitude;
}

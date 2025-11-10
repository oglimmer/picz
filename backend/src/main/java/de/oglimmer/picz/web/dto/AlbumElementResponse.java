package de.oglimmer.picz.web.dto;

import de.oglimmer.picz.db.AlbumElementType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AlbumElementResponse {

    private Long id;

    private AlbumElementType elementType;

    // data for all
    private String description;

    // data for image
    private String secretId;

    private Date creationDate;

    private Long orderNo;


    private String filename;

    private Double longitude; // marker longitude
    private Double latitude; // marker latitude
    private Double mapCenterLongitude;
    private Double mapCenterLatitude;
    private Double mapSpanLongitude;
    private Double mapSpanLatitude;
    private Integer zoomLevel;
}

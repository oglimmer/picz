package de.oglimmer.picz.web.dto;

import de.oglimmer.picz.db.AlbumElementType;
import lombok.Data;

import java.util.Date;

@Data
public class     ImageMetaResponse {

    private Long id;

    private AlbumElementType elementType;

    private String description;

    private Long orderNo;

    private String secretId;

    private String filename;

    private String contentHash;

    private Date creationDate;

    private Double longitude;

    private Double latitude;
}

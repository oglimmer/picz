package de.oglimmer.picz.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAlbumResponse {

    private Long id;
    private String description;
    private String secretId;

}

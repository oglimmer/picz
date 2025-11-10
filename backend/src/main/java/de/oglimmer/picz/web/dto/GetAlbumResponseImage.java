package de.oglimmer.picz.web.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GetAlbumResponseImage {

    private Long id;

    private List<AlbumElementResponse> albumElements;

    private String description;

    private String secretId;
}

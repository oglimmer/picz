package de.oglimmer.picz.web;

import de.oglimmer.picz.db.Album;
import de.oglimmer.picz.service.AlbumRepositoryService;
import de.oglimmer.picz.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/public/v1/album")
@AllArgsConstructor

public class PublicAlbumController {

    private AlbumRepositoryService albumRepositoryService;
    private ModelMapper modelMapper;

    @Operation(
            summary = "Returns information about an album by its secret id. These information contain all images including their secret ids."
    )
    @GetMapping
    public ResponseEntity<GetAlbumResponseImage> list(@RequestParam(value = "secretId") String secretId) {
        Album album = albumRepositoryService.findBySecretId(secretId);
        GetAlbumResponseImage response = modelMapper.map(album, GetAlbumResponseImage.class);
        response.getAlbumElements().sort(Comparator.comparing(AlbumElementResponse::getOrderNo));
        return ResponseEntity.ok(response);
    }

}

package de.oglimmer.picz.web;

import de.oglimmer.picz.service.ImageCreateService;
import de.oglimmer.picz.service.ImageService;
import de.oglimmer.picz.service.UploadException;
import de.oglimmer.picz.web.dto.ImagePatchRequest;
import de.oglimmer.picz.web.dto.UploadedImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/image")
@AllArgsConstructor
@Slf4j
public class ImageController {

    private ImageCreateService imageCreateService;
    private ImageService imageService;

    @Operation(
            summary = "Image upload endpoint, must be associated with an existing albumId",
            security = @SecurityRequirement(name = "OpenID")
    )
    @PostMapping
    public ResponseEntity<UploadedImage> create(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "albumId") long albumId
    ) {
        log.debug("upload image start: {}", image.getOriginalFilename());
        StopWatch stopWatch = new StopWatch(image.getOriginalFilename());
        stopWatch.start("upload image");
        ResponseEntity<UploadedImage> response;
        try {
            response = ResponseEntity.ok(imageCreateService.create(image, albumId));
        } catch (UploadException e) {
            response = ResponseEntity.status(e.getHttpStatus()).build();
        }
        stopWatch.stop();
        log.debug("upload image end: {}, {}", image.getOriginalFilename(), stopWatch);
        return response;
    }

    @Operation(
            summary = "Changing the images attributes. Currently supported description",
            security = @SecurityRequirement(name = "OpenID")
    )
    @PatchMapping("/{id}")
    public void patch(@PathVariable("id") long id, @RequestBody ImagePatchRequest imagePatchRequest) {
        imageService.patch(id, imagePatchRequest);
    }

    @Operation(
            summary = "Rotates the image by 90 degrees",
            security = @SecurityRequirement(name = "OpenID")
    )
    @PostMapping("/{id}/rotate")
    public String rotate(@PathVariable("id") long id) {
        return imageService.rotate(id).getSecretId();
    }

}

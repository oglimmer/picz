package de.oglimmer.picz.web;

import de.oglimmer.picz.db.AlbumElementType;
import de.oglimmer.picz.db.AlbumRepository;
import de.oglimmer.picz.db.CountElementByAlbum;
import de.oglimmer.picz.db.User;
import de.oglimmer.picz.service.QueueCountStats;
import de.oglimmer.picz.service.UserRepositoryService;
import de.oglimmer.picz.util.UserContextHolder;
import de.oglimmer.picz.web.dto.GetUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private UserRepositoryService userRepositoryService;
    private ModelMapper modelMapper;
    private QueueCountStats queueCountStats;
    private AlbumRepository albumRepository;

    @Operation(
            summary = "Returns the configuration for the currently authenticated user",
            security = @SecurityRequirement(name = "OpenID")
    )
    @GetMapping
    public GetUserResponse getUser() {
        User user = userRepositoryService.getUser().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        GetUserResponse response = modelMapper.map(user, GetUserResponse.class);
        
        Set<Long> albumListInUploadByUser = queueCountStats.getAlbumListInUploadByUser(user.getId());
        List<CountElementByAlbum> countElementByAlbum = albumRepository.findCountElementByAlbum(user.getId(), AlbumElementType.IMAGE);
        
        response.setProcessingAlbumIds(albumListInUploadByUser);
        response.setProcessingCounterByAlbum(queueCountStats.getProcessingCounterByAlbum(albumListInUploadByUser));
        response.setNumberOfImagesByAlbum(countElementByAlbum.stream()
                .collect(java.util.stream.Collectors.toMap(CountElementByAlbum::getId, CountElementByAlbum::getCount)));
        
        return response;
    }

    @Operation(
            summary = "Deletes the currently authenticated user and all of the associated albums and images",
            security = @SecurityRequirement(name = "OpenID")
    )
    @DeleteMapping
    public void deleteUser() {
        userRepositoryService.deleteUser();
    }

    @Operation(
            summary = "Accepts the TOS for the currently authenticated user",
            security = @SecurityRequirement(name = "OpenID")
    )
    @PostMapping("/accept-tos")
    public void acceptTos() {
        userRepositoryService.acceptTos();
    }

}

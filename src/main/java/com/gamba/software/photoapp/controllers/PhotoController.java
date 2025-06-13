package com.gamba.software.photoapp.controllers;

import com.gamba.software.photoapp.controllers.dto.PhotoResponse;
import com.gamba.software.photoapp.controllers.dto.PhotoUploadRequest;
import com.gamba.software.photoapp.repositories.enums.InteractionType;
import com.gamba.software.photoapp.repositories.models.Interaction;
import com.gamba.software.photoapp.repositories.models.Photo;
import com.gamba.software.photoapp.services.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/publish")
    public ResponseEntity<PhotoResponse> publishPhoto(@RequestBody PhotoUploadRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        PhotoResponse photo = photoService.publishPhoto(request.userId(), request.caption(), request.imageUrl(), request.privacy());

        return ResponseEntity.ok(photo);
    }

    @PostMapping("/{photoId}/tags/users")
    public ResponseEntity<Photo> tagUserInPhoto(
            @PathVariable UUID photoId,
            @RequestParam UUID userId) {

        return ResponseEntity.ok(photoService.tagUserInPhoto(photoId, userId));
    }

    @PostMapping("/{photoId}/tags")
    public ResponseEntity<Photo> addTagToPhoto(
            @PathVariable UUID photoId,
            @RequestParam String tagName) {

        return ResponseEntity.ok(photoService.addTagToPhoto(photoId, tagName));
    }

    @PostMapping("/{photoId}/interactions")
    public ResponseEntity<Interaction> addInteraction(
            @PathVariable UUID photoId,
            @RequestParam UUID userId,
            @RequestParam InteractionType type) {

        Interaction interaction = photoService.addInteraction(userId, photoId, type);

        return ResponseEntity.ok(interaction);
    }

    @DeleteMapping("/{photoId}/interactions")
    public ResponseEntity<Void> removeInteraction(
            @PathVariable UUID photoId,
            @RequestParam UUID userId,
            @RequestParam InteractionType type) {

        photoService.removeInteraction(userId, photoId, type);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<Photo> getPhoto(@PathVariable UUID photoId) {
        return ResponseEntity.ok(photoService.getPhotoById(photoId));
    }
}

package com.gamba.software.photoapp.photos.controllers;

import com.gamba.software.photoapp.photos.controllers.dto.PhotoResponse;
import com.gamba.software.photoapp.photos.controllers.dto.PhotoUploadRequest;
import com.gamba.software.photoapp.photos.repositories.enums.InteractionType; // Corrected
import com.gamba.software.photoapp.photos.repositories.models.Interaction;   // Corrected
import com.gamba.software.photoapp.photos.repositories.models.Photo;           // Corrected
import com.gamba.software.photoapp.photos.services.PhotoService; // Corrected
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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
    public ResponseEntity<PhotoResponse> publishPhoto(@RequestBody PhotoUploadRequest request, @AuthenticationPrincipal User user) {
        // Assuming user.getUsername() returns a string representation of UUID
        UUID userId = UUID.fromString(user.getUsername());
        PhotoResponse photo = photoService.publishPhoto(userId, request.caption(), request.imageUrl(), request.privacy());

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

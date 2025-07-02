package com.gamba.software.photoapp.photos.converters;

import com.gamba.software.photoapp.photos.controllers.dto.BasicUserResponse;
import com.gamba.software.photoapp.photos.controllers.dto.PhotoResponse;
import com.gamba.software.photoapp.photos.repositories.models.Photo;
import org.springframework.stereotype.Component;

@Component
public class PhotoResponseConverter {
    public PhotoResponse toResponse(Photo photo) {
        return new PhotoResponse(
                photo.getId(),
                photo.getCaption(),
                photo.getImageUrl(),
                photo.getUploadTime(),
                photo.getPrivacy(),
                photo.getLocation(),
                new BasicUserResponse(
                        photo.getUserId(), // Use userId directly
                        null,       // Username is not directly available
                        null        // Avatar URL is not directly available
                )
        );
    }
}

package com.gamba.software.photoapp.converters;

import com.gamba.software.photoapp.controllers.dto.BasicUserResponse;
import com.gamba.software.photoapp.controllers.dto.PhotoResponse;
import com.gamba.software.photoapp.repositories.models.Photo;
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
                        photo.getUserId(),
                        null,
                        null
                )
        );
    }
}

package com.gamba.software.photoapp.photos.controllers.dto;

import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType;

public record PhotoUploadRequest(
        String caption,
        String imageUrl,
        PrivacyType privacy
) {
}

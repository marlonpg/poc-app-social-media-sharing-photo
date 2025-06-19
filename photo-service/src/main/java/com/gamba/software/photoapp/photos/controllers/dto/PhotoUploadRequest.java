package com.gamba.software.photoapp.photos.controllers.dto;

import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType;

import java.util.UUID;

public record PhotoUploadRequest(
        UUID userId,
        String caption,
        String imageUrl,
        PrivacyType privacy
) {
}

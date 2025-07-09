package com.gamba.software.photoapp.controllers.dto;

import com.gamba.software.photoapp.repositories.enums.PrivacyType;

public record PhotoUploadRequest(
        String caption,
        String imageUrl,
        PrivacyType privacy
) {
}

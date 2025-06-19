package com.gamba.software.photoapp.photos.controllers.dto;

import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType;
import com.gamba.software.photoapp.photos.repositories.models.Location;

import java.time.Instant;
import java.util.UUID;

public record PhotoResponse(
        UUID id,
        String caption,
        String imageUrl,
        Instant uploadTime,
        PrivacyType privacy,
        Location location,
        com.gamba.software.photoapp.photos.controllers.dto.BasicUserResponse user // Using FQN for clarity during refactor
) {}

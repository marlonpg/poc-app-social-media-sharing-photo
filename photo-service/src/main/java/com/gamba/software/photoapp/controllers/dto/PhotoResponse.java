package com.gamba.software.photoapp.controllers.dto;

import com.gamba.software.photoapp.repositories.enums.PrivacyType;
import com.gamba.software.photoapp.repositories.models.Location;

import java.time.Instant;
import java.util.UUID;

public record PhotoResponse(
        UUID id,
        String caption,
        String imageUrl,
        Instant uploadTime,
        PrivacyType privacy,
        Location location,
        BasicUserResponse user // Using FQN for clarity during refactor
) {}

package com.gamba.software.photoapp.controllers.dto;

import java.util.UUID;

public record BasicUserResponse(
        UUID id,
        String username,
        String avatarUrl
) {}

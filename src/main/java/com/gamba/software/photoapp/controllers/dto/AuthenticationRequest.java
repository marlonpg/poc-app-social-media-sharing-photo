package com.gamba.software.photoapp.controllers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(@NotBlank @Email String email,
                                    @NotBlank String password) {
}
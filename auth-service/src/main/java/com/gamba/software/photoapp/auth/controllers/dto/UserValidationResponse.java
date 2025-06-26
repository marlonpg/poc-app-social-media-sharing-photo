package com.gamba.software.photoapp.auth.controllers.dto;

import java.util.Collection;
import java.util.List;

public record UserValidationResponse(String username, Collection<String> authorities) {
}

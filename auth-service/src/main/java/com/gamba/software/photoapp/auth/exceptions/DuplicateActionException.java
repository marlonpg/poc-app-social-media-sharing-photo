package com.gamba.software.photoapp.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateActionException extends RuntimeException {
    public DuplicateActionException(String actionDescription) {
        super("Duplicate action: " + actionDescription);
    }
}

package com.gamba.software.photoapp.photos.repositories.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Placeholder/stub for AppUser entity within PhotoService.
 * This will be replaced by a proper shared DTO or client library interaction later.
 */
@Entity
@Table(name = "app_user_stub") // Using a different table name to avoid conflicts if schemas were merged
public class AppUser {

    @Id
    private UUID id;
    private String username;
    private String avatarUrl; // Used by PhotoResponseConverter

    // Minimal constructor
    public AppUser(UUID id) {
        this.id = id;
    }

    public AppUser() {}


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

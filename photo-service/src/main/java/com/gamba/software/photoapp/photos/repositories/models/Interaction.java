package com.gamba.software.photoapp.photos.repositories.models;

import com.gamba.software.photoapp.photos.repositories.enums.InteractionType; // Corrected
// Assuming a local AppUser stub or a type that will be available in this package
import com.gamba.software.photoapp.photos.repositories.models.AppUser;        // Placeholder for AppUser
import com.gamba.software.photoapp.photos.repositories.models.Photo;           // Corrected
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Interaction {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private InteractionType type;

    private Instant timestamp;

    @ManyToOne
    private AppUser user;

    @ManyToOne
    private Photo photo;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InteractionType getType() {
        return type;
    }

    public void setType(InteractionType type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}

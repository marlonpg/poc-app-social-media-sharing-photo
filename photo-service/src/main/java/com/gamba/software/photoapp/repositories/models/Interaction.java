package com.gamba.software.photoapp.repositories.models;

import com.gamba.software.photoapp.repositories.enums.InteractionType; // Corrected
// Removed AppUser import
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

    @Column(name = "user_id", nullable = false) // Assuming user_id is mandatory
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "photo_id", nullable = false) // Assuming photo_id is mandatory
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }
}

package com.gamba.software.photoapp.photos.repositories.models;

import com.gamba.software.photoapp.photos.repositories.models.Photo; // Corrected
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Tag {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Photo> taggedPhotos = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Photo> getTaggedPhotos() {
        return taggedPhotos;
    }

    public void setTaggedPhotos(Set<Photo> taggedPhotos) {
        this.taggedPhotos = taggedPhotos;
    }
}

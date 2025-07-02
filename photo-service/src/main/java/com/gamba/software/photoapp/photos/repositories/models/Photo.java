package com.gamba.software.photoapp.photos.repositories.models;

import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType; // Corrected
// Removed AppUser import as it's deleted
import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
public class Photo {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String caption;
    private String imageUrl;
    private Instant uploadTime;

    @Enumerated(EnumType.STRING)
    private PrivacyType privacy;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "location_name"))
    })
    private Location location;

    @Column(name = "user_id", nullable = false) // Assuming user_id is mandatory
    private UUID userId;

    @ManyToMany
    @JoinTable(
            name = "photo_tag",
            joinColumns = @JoinColumn(name = "photo_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY) // Using ElementCollection for a set of UUIDs
    @CollectionTable(name = "photo_person_tag", joinColumns = @JoinColumn(name = "photo_id"))
    @Column(name = "user_id") // Name of the column in photo_person_tag table to store the tagged user IDs
    private Set<UUID> taggedUserIds = new HashSet<>();

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL)
    private List<Interaction> interactions = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Instant uploadTime) {
        this.uploadTime = uploadTime;
    }

    public PrivacyType getPrivacy() {
        return privacy;
    }

    public void setPrivacy(PrivacyType privacy) {
        this.privacy = privacy;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<UUID> getTaggedUserIds() {
        return taggedUserIds;
    }

    public void setTaggedUserIds(Set<UUID> taggedUserIds) {
        this.taggedUserIds = taggedUserIds;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Interaction> getInteractions() {
        return interactions;
    }

    public void setInteractions(List<Interaction> interactions) {
        this.interactions = interactions;
    }
}

package com.gamba.software.photoapp.photos.repositories.models;

import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType; // Corrected
import com.gamba.software.photoapp.photos.repositories.models.Location;   // Corrected
import com.gamba.software.photoapp.photos.repositories.models.Tag;        // Corrected
import com.gamba.software.photoapp.photos.repositories.models.Comment;    // Corrected
import com.gamba.software.photoapp.photos.repositories.models.Interaction; // Corrected
// Assuming a local AppUser stub or a type that will be available in this package
import com.gamba.software.photoapp.photos.repositories.models.AppUser;    // Placeholder for AppUser
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToMany
    @JoinTable(
            name = "photo_tag",
            joinColumns = @JoinColumn(name = "photo_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "photo_person_tag",
            joinColumns = @JoinColumn(name = "photo_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> taggedUsers = new HashSet<>();

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

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<AppUser> getTaggedUsers() {
        return taggedUsers;
    }

    public void setTaggedUsers(Set<AppUser> taggedUsers) {
        this.taggedUsers = taggedUsers;
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

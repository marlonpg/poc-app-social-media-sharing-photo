package com.gamba.software.photoapp.auth.repositories.models;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // Encrypted password

    private String avatarUrl;
    private String bio;

    // Security fields
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    // Relationships
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Relationships to Photo, Comment, Interaction are part of photo-service, not auth-service
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<Photo> photos = new ArrayList<>();
    //
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<Comment> comments = new ArrayList<>();
    //
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<Interaction> interactions = new ArrayList<>();

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // Getters and setters for Photo, Comment, Interaction removed
    // public List<Photo> getPhotos() {
    //     return photos;
    // }
    //
    // public void setPhotos(List<Photo> photos) {
    //     this.photos = photos;
    // }
    //
    // public List<Comment> getComments() {
    //     return comments;
    // }
    //
    // public void setComments(List<Comment> comments) {
    //     this.comments = comments;
    // }
    //
    // public List<Interaction> getInteractions() {
    //     return interactions;
    // }
    //
    // public void setInteractions(List<Interaction> interactions) {
    //     this.interactions = interactions;
    // }
}

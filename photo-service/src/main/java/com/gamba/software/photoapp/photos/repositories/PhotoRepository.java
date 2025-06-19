package com.gamba.software.photoapp.photos.repositories;

import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType; // Corrected
import com.gamba.software.photoapp.photos.repositories.models.AppUser;    // Placeholder for AppUser
import com.gamba.software.photoapp.photos.repositories.models.Photo;       // Corrected
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    List<Photo> findByUser(AppUser user);
    List<Photo> findByUserAndPrivacy(AppUser user, PrivacyType privacy);
    List<Photo> findByTaggedUsers(AppUser user);
    List<Photo> findByTagsName(String tagName);
}

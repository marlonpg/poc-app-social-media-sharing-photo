package com.gamba.software.photoapp.repositories;

import com.gamba.software.photoapp.repositories.enums.PrivacyType; // Corrected
// Removed AppUser import
import com.gamba.software.photoapp.repositories.models.Photo;       // Corrected
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    List<Photo> findByUserId(UUID userId);
    List<Photo> findByUserIdAndPrivacy(UUID userId, PrivacyType privacy);
    List<Photo> findByTaggedUserIdsContains(UUID userId); // Updated for ElementCollection
    List<Photo> findByTagsName(String tagName);
}

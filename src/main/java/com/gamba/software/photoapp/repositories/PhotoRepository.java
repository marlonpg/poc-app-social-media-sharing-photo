package com.gamba.software.photoapp.repositories;

import com.gamba.software.photoapp.repositories.enums.PrivacyType;
import com.gamba.software.photoapp.repositories.models.AppUser;
import com.gamba.software.photoapp.repositories.models.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {
    List<Photo> findByUser(AppUser user);
    List<Photo> findByUserAndPrivacy(AppUser user, PrivacyType privacy);
    List<Photo> findByTaggedUsers(AppUser user);
    List<Photo> findByTagsName(String tagName);
}
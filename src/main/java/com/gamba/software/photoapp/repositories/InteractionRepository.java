package com.gamba.software.photoapp.repositories;

import com.gamba.software.photoapp.repositories.enums.InteractionType;
import com.gamba.software.photoapp.repositories.models.AppUser;
import com.gamba.software.photoapp.repositories.models.Interaction;
import com.gamba.software.photoapp.repositories.models.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    Optional<Interaction> findByUserAndPhotoAndType(AppUser user, Photo photo, InteractionType type);
    Long countByPhotoAndType(Photo photo, InteractionType type);
}

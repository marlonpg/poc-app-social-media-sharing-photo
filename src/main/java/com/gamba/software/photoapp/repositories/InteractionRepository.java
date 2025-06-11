package com.gamba.software.photoapp.repositories;

import com.gamba.software.photoapp.repositories.enums.InteractionType;
import com.gamba.software.photoapp.repositories.models.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
    Optional<Interaction> findByUserIdAndPhotoIdAndType(UUID userId, UUID photoId, InteractionType type);
    List<Interaction> findByPhotoIdAndType(UUID photoId, InteractionType type);
    boolean existsByUserIdAndPhotoIdAndType(UUID userId, UUID photoId, InteractionType type);
}

package com.gamba.software.photoapp.photos.repositories;

import com.gamba.software.photoapp.photos.repositories.models.Tag; // Corrected
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    // Find photos by tag name with pagination
    // Page<Tag> findByNameContaining(String name, Pageable pageable);
}

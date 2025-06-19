package com.gamba.software.photoapp.photos.repositories;

import com.gamba.software.photoapp.photos.repositories.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Placeholder/stub for AppUserRepository within PhotoService.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    // Define methods used by PhotoService, if any, beyond JpaRepository methods
    // For example, if PhotoService used findByUsername, it would be declared here.
    // Currently, PhotoService uses findById, which is provided by JpaRepository.
    // However, the AppUser type it refers to is the local stub.
    Optional<AppUser> findById(UUID id); // Explicitly define to ensure it uses the local AppUser stub
}

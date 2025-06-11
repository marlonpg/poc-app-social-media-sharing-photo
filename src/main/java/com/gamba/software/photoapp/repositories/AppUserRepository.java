package com.gamba.software.photoapp.repositories;

import com.gamba.software.photoapp.repositories.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
}
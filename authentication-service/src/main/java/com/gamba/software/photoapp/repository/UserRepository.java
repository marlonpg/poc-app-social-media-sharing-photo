package com.gamba.software.photoapp.repository;

import com.gamba.software.photoapp.repository.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.nio.channels.FileChannel;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository  extends JpaRepository<User, UUID> {
    Optional<User> existsByUsername(String username);

    Optional<User> findByUsername(String username);
}

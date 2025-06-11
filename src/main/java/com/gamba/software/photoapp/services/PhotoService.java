package com.gamba.software.photoapp.services;

import com.gamba.software.photoapp.exceptions.UserNotFoundException;
import com.gamba.software.photoapp.repositories.AppUserRepository;
import com.gamba.software.photoapp.repositories.PhotoRepository;
import com.gamba.software.photoapp.repositories.TagRepository;
import com.gamba.software.photoapp.repositories.enums.InteractionType;
import com.gamba.software.photoapp.repositories.enums.PrivacyType;
import com.gamba.software.photoapp.repositories.models.AppUser;
import com.gamba.software.photoapp.repositories.models.Interaction;
import com.gamba.software.photoapp.repositories.models.Photo;

import java.time.Instant;
import java.util.UUID;

public class PhotoService {

    private PhotoRepository photoRepository;
    private AppUserRepository appUserRepository;
    private TagRepository tagRepository;

    public PhotoService(PhotoRepository photoRepository, AppUserRepository appUserRepository, TagRepository tagRepository) {
        this.photoRepository = photoRepository;
        this.appUserRepository = appUserRepository;
        this.tagRepository = tagRepository;
    }


    public Photo uploadPhoto(UUID userId, String caption, String imageUrl, PrivacyType privacy) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setImageUrl(imageUrl);
        photo.setPrivacy(privacy);
        photo.setUploadTime(Instant.now());
        photo.setUser(user);

        return photoRepository.save(photo);
    }

    public Photo tagUserInPhoto(UUID photoId, UUID userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId));
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        photo.getTaggedUsers().add(user);
        return photoRepository.save(photo);
    }

    public Interaction addInteraction(UUID userId, UUID photoId, InteractionType type) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoNotFoundException(photoId));

        // Check for existing interaction
        interactionRepository.findByUserAndPhotoAndType(user, photo, type)
                .ifPresent(interaction -> {
                    throw new DuplicateInteractionException();
                });

        Interaction interaction = new Interaction();
        interaction.setType(type);
        interaction.setUser(user);
        interaction.setPhoto(photo);
        interaction.setTimestamp(Instant.now());

        return interactionRepository.save(interaction);
    }
}

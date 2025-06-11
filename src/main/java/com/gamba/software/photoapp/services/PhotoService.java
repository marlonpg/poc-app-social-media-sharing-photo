package com.gamba.software.photoapp.services;

import com.gamba.software.photoapp.controllers.dto.PhotoResponse;
import com.gamba.software.photoapp.converters.PhotoResponseConverter;
import com.gamba.software.photoapp.exceptions.BusinessRuleException;
import com.gamba.software.photoapp.exceptions.ResourceAlreadyExistsException;
import com.gamba.software.photoapp.exceptions.ResourceNotFoundException;
import com.gamba.software.photoapp.repositories.AppUserRepository;
import com.gamba.software.photoapp.repositories.InteractionRepository;
import com.gamba.software.photoapp.repositories.PhotoRepository;
import com.gamba.software.photoapp.repositories.TagRepository;
import com.gamba.software.photoapp.repositories.enums.InteractionType;
import com.gamba.software.photoapp.repositories.enums.PrivacyType;
import com.gamba.software.photoapp.repositories.models.AppUser;
import com.gamba.software.photoapp.repositories.models.Interaction;
import com.gamba.software.photoapp.repositories.models.Photo;
import com.gamba.software.photoapp.repositories.models.Tag;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AppUserRepository appUserRepository;
    private final TagRepository tagRepository;
    private final InteractionRepository interactionRepository;
    private final PhotoResponseConverter photoResponseConverter;

    public PhotoService(PhotoRepository photoRepository,
                        AppUserRepository appUserRepository,
                        TagRepository tagRepository,
                        InteractionRepository interactionRepository,
                        PhotoResponseConverter photoResponseConverter) {
        this.photoRepository = photoRepository;
        this.appUserRepository = appUserRepository;
        this.tagRepository = tagRepository;
        this.interactionRepository = interactionRepository;
        this.photoResponseConverter = photoResponseConverter;
    }

    public Photo getPhotoById(UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));
    }

    public PhotoResponse uploadPhoto(UUID userId, String caption, String imageUrl, PrivacyType privacy) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setImageUrl(imageUrl);
        photo.setPrivacy(privacy);
        photo.setUploadTime(Instant.now());
        photo.setUser(user);

        return photoResponseConverter.toResponse(photoRepository.save(photo));
    }

    public Photo tagUserInPhoto(UUID photoId, UUID userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (photo.getTaggedUsers().contains(user)) {
            throw new ResourceAlreadyExistsException("User tag", "user_id", userId);
        }

        photo.getTaggedUsers().add(user);
        return photoRepository.save(photo);
    }

    public Photo addTagToPhoto(UUID photoId, String tagName) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        Optional<Tag> existingTag = tagRepository.findByName(tagName);
        Tag tag = existingTag.orElseGet(() -> {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            return tagRepository.save(newTag);
        });

        if (photo.getTags().contains(tag)) {
            throw new ResourceAlreadyExistsException("Photo tag", "tag_name", tagName);
        }

        photo.getTags().add(tag);
        return photoRepository.save(photo);
    }

    public Interaction addInteraction(UUID userId, UUID photoId, InteractionType type) {
        Optional<AppUser> appUser = appUserRepository.findById(userId);
        if (appUser.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        Optional<Photo> photo = photoRepository.findById(photoId);
        if (photo.isEmpty()) {
            throw new ResourceNotFoundException("Photo", "id", photoId);
        }

        if (interactionRepository.existsByUserIdAndPhotoIdAndType(userId, photoId, type)) {
            throw new BusinessRuleException(
                    String.format("User %s already performed %s on photo %s", userId, type, photoId)
            );
        }

        Interaction interaction = new Interaction();
        interaction.setType(type);
        interaction.setUser(appUser.get());
        interaction.setPhoto(photo.get());
        interaction.setTimestamp(Instant.now());

        return interactionRepository.save(interaction);
    }

    public void removeInteraction(UUID userId, UUID photoId, InteractionType type) {
        Interaction interaction = interactionRepository
                .findByUserIdAndPhotoIdAndType(userId, photoId, type)
                .orElseThrow(() -> new ResourceNotFoundException("Interaction",
                        String.format("user_id=%s, photo_id=%s, type=%s", userId, photoId, type), null
                ));

        interactionRepository.delete(interaction);
    }
}

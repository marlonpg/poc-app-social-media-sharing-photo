package com.gamba.software.photoapp.photos.services;

import com.gamba.software.photoapp.photos.controllers.dto.PhotoResponse;
import com.gamba.software.photoapp.photos.converters.PhotoResponseConverter;
import com.gamba.software.photoapp.photos.exceptions.BusinessRuleException;
import com.gamba.software.photoapp.auth.exceptions.ResourceAlreadyExistsException; // from auth-service
import com.gamba.software.photoapp.auth.exceptions.ResourceNotFoundException;   // from auth-service
// Removed AppUserRepository and AppUser imports
import com.gamba.software.photoapp.photos.repositories.InteractionRepository;
import com.gamba.software.photoapp.photos.repositories.PhotoRepository;
import com.gamba.software.photoapp.photos.repositories.TagRepository;
import com.gamba.software.photoapp.photos.repositories.enums.InteractionType;
import com.gamba.software.photoapp.photos.repositories.enums.PrivacyType;
import com.gamba.software.photoapp.photos.repositories.models.Interaction;
import com.gamba.software.photoapp.photos.repositories.models.Photo;
import com.gamba.software.photoapp.photos.repositories.models.Tag;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    // Removed AppUserRepository
    private final TagRepository tagRepository;
    private final InteractionRepository interactionRepository;
    private final PhotoResponseConverter photoResponseConverter;

    public PhotoService(PhotoRepository photoRepository,
                        // Removed AppUserRepository from constructor
                        TagRepository tagRepository,
                        InteractionRepository interactionRepository,
                        PhotoResponseConverter photoResponseConverter) {
        this.photoRepository = photoRepository;
        this.tagRepository = tagRepository;
        this.interactionRepository = interactionRepository;
        this.photoResponseConverter = photoResponseConverter;
    }

    public Photo getPhotoById(UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));
    }

    public PhotoResponse publishPhoto(UUID userId, String caption, String imageUrl, PrivacyType privacy) {
        // User existence is assumed to be validated by the authentication mechanism (JWT)
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setImageUrl(imageUrl);
        photo.setPrivacy(privacy);
        photo.setUploadTime(Instant.now());
        photo.setUserId(userId); // Set userId directly

        return photoResponseConverter.toResponse(photoRepository.save(photo));
    }

    public Photo tagUserInPhoto(UUID photoId, UUID taggedUserId) { // Renamed userId to taggedUserId for clarity
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        // User existence of taggedUserId is an external concern, photo-service just stores the ID
        if (photo.getTaggedUserIds().contains(taggedUserId)) {
            throw new ResourceAlreadyExistsException("User tag", "user_id", taggedUserId);
        }

        photo.getTaggedUserIds().add(taggedUserId);
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
        // User existence (userId) is assumed validated by JWT.
        // Photo existence check:
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        if (interactionRepository.existsByUserIdAndPhotoIdAndType(userId, photoId, type)) {
            throw new BusinessRuleException(
                    String.format("User %s already performed %s on photo %s", userId, type, photoId)
            );
        }

        Interaction interaction = new Interaction();
        interaction.setType(type);
        interaction.setUserId(userId); // Set userId directly
        interaction.setPhoto(photo);   // Set the fetched photo object
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

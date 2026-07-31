package com.cqu.cupid.profile;

import java.io.InputStream;
import java.util.Optional;

public class ProfileService {

    private final ProfileRepository repository;
    private final EthicsConfig ethicsConfig;
    private final ProfilePictureStorage pictureStorage;
    private final ProfileValidator validator;

    public ProfileService(ProfileRepository repository, EthicsConfig ethicsConfig,
                           ProfilePictureStorage pictureStorage, ProfileValidator validator) {
        this.repository = repository;
        this.ethicsConfig = ethicsConfig;
        this.pictureStorage = pictureStorage;
        this.validator = validator;
    }

    public Profile createProfile(Profile profile) {
        validator.validate(profile);
        return repository.save(profile);
    }

    public void deleteProfile(Long id) {
        if (ethicsConfig.isKeepEthicsEnabled()) {
            repository.softDelete(id);
        } else {
            repository.hardDelete(id);
        }
    }

    public Profile uploadProfilePicture(Long profileId, InputStream imageData, String originalFilename) {
        Optional<Profile> existing = repository.findById(profileId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("No profile found with id " + profileId);
        }

        Profile profile = existing.get();
        String storedPath = pictureStorage.store(imageData, originalFilename);
        profile.setProfilePicture(storedPath);

        return repository.save(profile);
    }

}
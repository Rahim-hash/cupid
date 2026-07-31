package com.cqu.cupid.profile;

public class ProfileService {

    private final ProfileRepository repository;
    private final EthicsConfig ethicsConfig;
    private final ProfilePictureStorage pictureStorage;

    public ProfileService(ProfileRepository repository, EthicsConfig ethicsConfig,
                           ProfilePictureStorage pictureStorage) {
        this.repository = repository;
        this.ethicsConfig = ethicsConfig;
        this.pictureStorage = pictureStorage;
    }

    public Profile createProfile(Profile profile) {
        return repository.save(profile);
    }

    public void deleteProfile(Long id) {
        if (ethicsConfig.isKeepEthicsEnabled()) {
            repository.softDelete(id);
        } else {
            repository.hardDelete(id);
        }
    }

}
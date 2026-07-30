package com.cqu.cupid.profile;

public class ProfileService {

    private final ProfileRepository repository;
    private final EthicsConfig ethicsConfig;

    public ProfileService(ProfileRepository repository, EthicsConfig ethicsConfig) {
        this.repository = repository;
        this.ethicsConfig = ethicsConfig;
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
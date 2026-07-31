package com.cqu.cupid.profile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ProfileServiceTest {

    @Test
    void deleteProfileSoftDeletesWhenEthicsEnabled() {
        ProfileRepository repository = new JdbcProfileRepository();
        EthicsConfig ethicsConfig = new EthicsConfig();
        ethicsConfig.setKeepEthicsEnabled(true);
        ProfilePictureStorage pictureStorage = new ProfilePictureStorage();
        ProfileService service = new ProfileService(repository, ethicsConfig, pictureStorage);

        Profile saved = service.createProfile(new Profile("Dana", 29, "Runs marathons"));
        service.deleteProfile(saved.getId());

        Optional<Profile> foundNormally = repository.findById(saved.getId());
        Optional<Profile> foundIncludingDeleted = repository.findByIdIncludingDeleted(saved.getId());

        assertFalse(foundNormally.isPresent());
        assertTrue(foundIncludingDeleted.isPresent());
    }

    @Test
    void deleteProfileHardDeletesWhenEthicsDisabled() {
        ProfileRepository repository = new JdbcProfileRepository();
        EthicsConfig ethicsConfig = new EthicsConfig();
        ethicsConfig.setKeepEthicsEnabled(false);
        ProfilePictureStorage pictureStorage = new ProfilePictureStorage();
        ProfileService service = new ProfileService(repository, ethicsConfig, pictureStorage);

        Profile saved = service.createProfile(new Profile("Evan", 37, "Collects vinyl"));
        service.deleteProfile(saved.getId());

        Optional<Profile> foundIncludingDeleted = repository.findByIdIncludingDeleted(saved.getId());

        assertFalse(foundIncludingDeleted.isPresent());
    }
    @Test
    void uploadProfilePictureUpdatesProfileWithStoredPath() throws Exception {
        ProfileRepository repository = new JdbcProfileRepository();
        EthicsConfig ethicsConfig = new EthicsConfig();
        ProfilePictureStorage pictureStorage = new ProfilePictureStorage();
        ProfileService service = new ProfileService(repository, ethicsConfig, pictureStorage);

        Profile saved = service.createProfile(new Profile("Farah", 26, "Loves photography"));
        InputStream fakeImageData = new ByteArrayInputStream("fake image bytes".getBytes());

        Profile updated = service.uploadProfilePicture(saved.getId(), fakeImageData, "avatar.png");

        assertNotNull(updated.getProfilePicture());
        assertTrue(updated.getProfilePicture().endsWith(".png"));
    }

}
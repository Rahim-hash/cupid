package com.cqu.cupid.profile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class JdbcProfileRepositoryTest {

    @Test
    void saveAssignsGeneratedId() {
        ProfileRepository repository = new JdbcProfileRepository();
        Profile profile = new Profile("Alice", 28, "Loves hiking");

        Profile saved = repository.save(profile);

        assertNotNull(saved.getId());
    }
    @Test
    void findByIdReturnsSavedProfile() {
        ProfileRepository repository = new JdbcProfileRepository();
        Profile profile = new Profile("Bob", 34, "Enjoys chess");
        Profile saved = repository.save(profile);

        Optional<Profile> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
    }
    @Test
    void softDeleteHidesProfileFromFindById() {
        ProfileRepository repository = new JdbcProfileRepository();
        Profile profile = new Profile("Carol", 41, "Plays guitar");
        Profile saved = repository.save(profile);

        repository.softDelete(saved.getId());
        Optional<Profile> found = repository.findById(saved.getId());

        assertFalse(found.isPresent());
    }

}
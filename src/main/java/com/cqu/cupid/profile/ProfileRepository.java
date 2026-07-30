package com.cqu.cupid.profile;

import java.util.Optional;

/**
 * Persistence abstraction for Profile entities.
 * Design: docs/uml-profile.png
 * Implements: FR_Profile, FR_Profile_Fetch, FR_Profile_Keep_Ethics
 */
public interface ProfileRepository {

    Profile save(Profile profile);

    Optional<Profile> findById(Long id);
    Optional<Profile> findByIdIncludingDeleted(Long id);

    void softDelete(Long id);
    void hardDelete(Long id);
}
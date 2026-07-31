package com.cqu.cupid.profile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class ProfileValidatorTest {

    private final ProfileValidator validator = new ProfileValidator();

    @Test
    void validProfilePassesValidation() {
        Profile profile = new Profile("Grace", 25, "Enjoys painting");
        assertDoesNotThrow(() -> validator.validate(profile));
    }

    @Test
    void blankNameThrowsException() {
        Profile profile = new Profile("", 25, "bio");
        assertThrows(IllegalArgumentException.class, () -> validator.validate(profile));
    }

    @Test
    void underageThrowsException() {
        Profile profile = new Profile("Henry", 16, "bio");
        assertThrows(IllegalArgumentException.class, () -> validator.validate(profile));
    }

    @Test
    void bioTooLongThrowsException() {
        String longBio = "a".repeat(501);
        Profile profile = new Profile("Ivy", 25, longBio);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(profile));
    }

}
package com.cqu.cupid.profile;

public class ProfileValidator {

    private static final int MAX_BIO_LENGTH = 500;

    public void validate(Profile profile) {
        if (profile.getName() == null || profile.getName().isBlank()) {
            throw new IllegalArgumentException("Profile name must not be blank");
        }
        if (profile.getAge() == null || profile.getAge() < 18 || profile.getAge() > 120) {
            throw new IllegalArgumentException("Profile age must be between 18 and 120");
        }
        if (profile.getBio() != null && profile.getBio().length() > MAX_BIO_LENGTH) {
            throw new IllegalArgumentException("Profile bio must not exceed " + MAX_BIO_LENGTH + " characters");
        }
    }

}
package com.cqu.cupid.profile;

/**
 * Domain entity for a Cupid user profile.
 * Design: docs/erd-profile.png, docs/uml-profile.png
 * Implements: FR_Profile, FR_Profile_Picture, FR_Profile_Keep_Ethics (soft-delete via `deleted` flag)
 */
public class Profile {

    private Long id;
    private String name;
    private Integer age;
    private String bio;
    private String profilePicture;
    private boolean deleted;

    public Profile() {
    }

    public Profile(String name, Integer age, String bio) {
        this.name = name;
        this.age = age;
        this.bio = bio;
        this.deleted = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Profile{id=" + id + ", name='" + name + "', age=" + age
                + ", deleted=" + deleted + "}";
    }
}
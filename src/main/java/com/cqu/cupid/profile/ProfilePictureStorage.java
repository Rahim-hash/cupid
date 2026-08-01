package com.cqu.cupid.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class ProfilePictureStorage {

    private static final String UPLOAD_DIR = "uploads/profile-pictures";
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");

    public String store(InputStream imageData, String originalFilename) {
        String extension = getExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                "Invalid file type: " + extension + ". Allowed types: " + ALLOWED_EXTENSIONS);
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);

            String storedFilename = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(storedFilename);

            Files.copy(imageData, targetPath);

            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store profile picture", e);
        }
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot >= 0 ? filename.substring(lastDot) : "";
    }

}
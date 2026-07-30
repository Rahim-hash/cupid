package com.cqu.cupid.profile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ProfilePictureStorageTest {

    @Test
    void storeSavesFileAndReturnsValidPath() throws Exception {
        ProfilePictureStorage storage = new ProfilePictureStorage();
        InputStream fakeImageData = new ByteArrayInputStream("fake image bytes".getBytes());

        String savedPath = storage.store(fakeImageData, "photo.jpg");

        Path path = Paths.get(savedPath);
        assertTrue(Files.exists(path));
        assertTrue(savedPath.endsWith(".jpg"));
    }

}
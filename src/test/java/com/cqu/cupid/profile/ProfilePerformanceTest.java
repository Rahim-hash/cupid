package com.cqu.cupid.profile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ProfilePerformanceTest {

    @Test
    void supportsRequiredCapacityWithAcceptableResponseTime() {
        ProfileRepository repository = new JdbcProfileRepository();
        EthicsConfig ethicsConfig = new EthicsConfig();
        ProfilePictureStorage pictureStorage = new ProfilePictureStorage();
        ProfileValidator validator = new ProfileValidator();
        ProfileService service = new ProfileService(repository, ethicsConfig, pictureStorage, validator);

        int numberOfProfiles = 100;
        long totalTimeMillis = 0;

        for (int i = 0; i < numberOfProfiles; i++) {
            long start = System.nanoTime();
            service.createProfile(new Profile("User" + i, 25, "Test bio " + i));
            long end = System.nanoTime();
            totalTimeMillis += (end - start) / 1_000_000;
        }

        double averageResponseTimeMillis = (double) totalTimeMillis / numberOfProfiles;

        System.out.println("=== Performance Test Results ===");
        System.out.println("Profiles created: " + numberOfProfiles);
        System.out.println("Total time (ms): " + totalTimeMillis);
        System.out.println("Average response time (ms): " + averageResponseTimeMillis);

        assertTrue(averageResponseTimeMillis < 100,
                "Average response time should be under 100ms per profile, was: " + averageResponseTimeMillis);
    }

}
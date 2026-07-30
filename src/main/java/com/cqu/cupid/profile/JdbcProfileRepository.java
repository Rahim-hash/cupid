package com.cqu.cupid.profile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class JdbcProfileRepository implements ProfileRepository {

    private static final String DB_URL = "jdbc:h2:mem:cupiddb;DB_CLOSE_DELAY=-1";

    public JdbcProfileRepository() {
        createTableIfNotExists();
        
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, "sa", "");
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS PROFILE ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "age INT NOT NULL, "
                + "bio VARCHAR(500), "
                + "profile_picture VARCHAR(255), "
                + "deleted BOOLEAN NOT NULL DEFAULT FALSE"
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create PROFILE table", e);
        }
    }
    @Override
    public Profile save(Profile profile) {
        String sql = "INSERT INTO PROFILE (name, age, bio, profile_picture, deleted) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, profile.getName());
            stmt.setInt(2, profile.getAge());
            stmt.setString(3, profile.getBio());
            stmt.setString(4, profile.getProfilePicture());
            stmt.setBoolean(5, profile.isDeleted());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    profile.setId(generatedKeys.getLong(1));
                }
            }

            return profile;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save profile", e);
        }
    }
    @Override
    public Optional<Profile> findById(Long id) {
        String sql = "SELECT id, name, age, bio, profile_picture, deleted "
                + "FROM PROFILE WHERE id = ? AND deleted = FALSE";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Profile profile = new Profile();
                    profile.setId(rs.getLong("id"));
                    profile.setName(rs.getString("name"));
                    profile.setAge(rs.getInt("age"));
                    profile.setBio(rs.getString("bio"));
                    profile.setProfilePicture(rs.getString("profile_picture"));
                    profile.setDeleted(rs.getBoolean("deleted"));
                    return Optional.of(profile);
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find profile with id " + id, e);
        }
    }
    @Override
    public void softDelete(Long id) {
        String sql = "UPDATE PROFILE SET deleted = TRUE WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft-delete profile with id " + id, e);
        }
    }

}
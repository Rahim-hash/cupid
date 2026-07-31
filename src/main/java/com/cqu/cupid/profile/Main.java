package com.cqu.cupid.profile;

public class Main {
    public static void main(String[] args) throws Exception {
        ProfileRepository repository = new JdbcProfileRepository();
        EthicsConfig ethicsConfig = new EthicsConfig();
        ProfilePictureStorage pictureStorage = new ProfilePictureStorage();
        ProfileValidator validator = new ProfileValidator();
        ProfileService service = new ProfileService(repository, ethicsConfig, pictureStorage, validator);

        ProfileWebServer server = new ProfileWebServer(service);
        server.start(8080);
    }
}
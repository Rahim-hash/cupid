package com.cqu.cupid.profile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ProfileWebServer {

    private final ProfileService profileService;

    private static final String STYLE = "<style>"
            + "body{font-family:Arial,sans-serif;max-width:600px;margin:40px auto;padding:0 20px;color:#333;}"
            + "h1{color:#e63946;}"
            + "h2{color:#457b9d;margin-top:30px;}"
            + "input,textarea{padding:8px;margin:4px 0;width:100%;box-sizing:border-box;border:1px solid #ccc;border-radius:4px;}"
            + "button{background:#e63946;color:white;border:none;padding:10px 20px;border-radius:4px;cursor:pointer;margin-top:8px;}"
            + "button:hover{background:#d62828;}"
            + "a{color:#457b9d;}"
            + "form{margin-bottom:20px;}"
            + ".avatar{width:150px;height:150px;border-radius:50%;object-fit:cover;border:3px solid #457b9d;}"
            + "</style>";

    public ProfileWebServer(ProfileService profileService) {
        this.profileService = profileService;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleHome);
        server.createContext("/create", this::handleCreate);
        server.createContext("/view", this::handleView);
        server.createContext("/delete", this::handleDelete);
        server.createContext("/upload", this::handleUpload);
        server.createContext("/image", this::handleImage);
        server.setExecutor(null);
        server.start();
        System.out.println("Cupid Profile server running at http://localhost:" + port);
    }

    private void handleHome(HttpExchange exchange) throws IOException {
        String html = "<h1>Cupid - Profile</h1>"
                + "<h2>Create Profile</h2>"
                + "<form method='POST' action='/create'>"
                + "Name: <input type='text' name='name'><br>"
                + "Age: <input type='text' name='age'><br>"
                + "Bio: <textarea name='bio'></textarea><br>"
                + "<button type='submit'>Create</button>"
                + "</form>"
                + "<h2>View Profile</h2>"
                + "<form method='GET' action='/view'>"
                + "Profile ID: <input type='text' name='id'><br>"
                + "<button type='submit'>Fetch</button>"
                + "</form>"
                + "<h2>Delete Profile</h2>"
                + "<form method='POST' action='/delete'>"
                + "Profile ID: <input type='text' name='id'><br>"
                + "<button type='submit'>Delete</button>"
                + "</form>"
                + "<h2>Upload Profile Picture</h2>"
                + "<form method='POST' action='/upload' enctype='multipart/form-data'>"
                + "Profile ID: <input type='text' name='id'><br>"
                + "Picture: <input type='file' name='picture'><br>"
                + "<button type='submit'>Upload</button>"
                + "</form>";

        sendResponse(exchange, 200, html);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> params = parseFormBody(exchange);
        try {
            String name = params.getOrDefault("name", "");
            Integer age = parseAge(params.get("age"));
            String bio = params.getOrDefault("bio", "");

            Profile profile = new Profile(name, age, bio);
            Profile saved = profileService.createProfile(profile);

            String html = "<h1>Profile Created</h1>"
                    + "<p>ID: " + saved.getId() + "</p>"
                    + "<p>Name: " + escapeHtml(saved.getName()) + "</p>"
                    + "<a href='/'>Back</a>";
            sendResponse(exchange, 200, html);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "<h1>Invalid input</h1><p>" + escapeHtml(e.getMessage()) + "</p><a href='/'>Back</a>");
        }
    }

    private void handleView(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryString(exchange.getRequestURI().getQuery());
        String idParam = params.get("id");

        if (idParam == null) {
            sendResponse(exchange, 400, "Missing id parameter");
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            Optional<Profile> profile = profileServiceFind(id);

            if (profile.isPresent()) {
                Profile p = profile.get();
                String pictureHtml = (p.getProfilePicture() != null && !p.getProfilePicture().isBlank())
                        ? "<p><img src='/image?id=" + p.getId() + "' class='avatar'></p>"
                        : "<p>Picture: none uploaded yet</p>";

                String html = "<h1>Profile</h1>"
                        + "<p>ID: " + p.getId() + "</p>"
                        + "<p>Name: " + escapeHtml(p.getName()) + "</p>"
                        + "<p>Age: " + p.getAge() + "</p>"
                        + "<p>Bio: " + escapeHtml(p.getBio()) + "</p>"
                        + pictureHtml
                        + "<a href='/'>Back</a>";
                sendResponse(exchange, 200, html);
            } else {
                sendResponse(exchange, 404, "<h1>Not found</h1><a href='/'>Back</a>");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid id format");
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        Map<String, String> params = parseFormBody(exchange);
        try {
            Long id = Long.parseLong(params.get("id"));
            profileService.deleteProfile(id);
            sendResponse(exchange, 200,
                "<h1>Profile Deleted</h1><p>ID " + id + " deleted.</p><a href='/'>Back</a>");
        } catch (Exception e) {
            sendResponse(exchange, 400,
                "<h1>Error</h1><p>" + escapeHtml(e.getMessage()) + "</p><a href='/'>Back</a>");
        }
    }

    private void handleUpload(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        try {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            String boundary = "--" + contentType.split("boundary=")[1];
            byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
            String body = new String(bodyBytes, StandardCharsets.ISO_8859_1);

            String[] parts = body.split(boundary);

            Long id = null;
            byte[] fileBytes = null;
            String filename = "upload.jpg";

            for (String part : parts) {
                if (part.contains("name=\"id\"")) {
                    int start = part.indexOf("\r\n\r\n") + 4;
                    int end = part.lastIndexOf("\r\n");
                    if (start < end) {
                        id = Long.parseLong(part.substring(start, end).trim());
                    }
                }
                if (part.contains("name=\"picture\"")) {
                    if (part.contains("filename=\"")) {
                        int fnStart = part.indexOf("filename=\"") + 10;
                        int fnEnd = part.indexOf("\"", fnStart);
                        filename = part.substring(fnStart, fnEnd);
                    }
                    int start = part.indexOf("\r\n\r\n") + 4;
                    int end = part.lastIndexOf("\r\n");
                    if (start < end) {
                        String fileContent = part.substring(start, end);
                        fileBytes = fileContent.getBytes(StandardCharsets.ISO_8859_1);
                    }
                }
            }

            if (id == null || fileBytes == null || fileBytes.length == 0) {
                sendResponse(exchange, 400, "<h1>Error</h1><p>Missing id or file</p><a href='/'>Back</a>");
                return;
            }

            InputStream imageData = new ByteArrayInputStream(fileBytes);
            Profile updated = profileService.uploadProfilePicture(id, imageData, filename);

            String html = "<h1>Picture Uploaded</h1>"
                    + "<p>Stored at: " + escapeHtml(updated.getProfilePicture()) + "</p>"
                    + "<a href='/'>Back</a>";
            sendResponse(exchange, 200, html);
        } catch (Exception e) {
            sendResponse(exchange, 400, "<h1>Error</h1><p>" + escapeHtml(e.getMessage()) + "</p><a href='/'>Back</a>");
        }
    }

    private void handleImage(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryString(exchange.getRequestURI().getQuery());
        String idParam = params.get("id");

        if (idParam == null) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            Optional<Profile> profile = profileServiceFind(id);

            if (profile.isEmpty() || profile.get().getProfilePicture() == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            Path imagePath = Paths.get(profile.get().getProfilePicture());
            if (!Files.exists(imagePath)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            byte[] imageBytes = Files.readAllBytes(imagePath);
            String contentType = guessContentType(imagePath.toString());

            exchange.getResponseHeaders().put("Content-Type", List.of(contentType));
            exchange.sendResponseHeaders(200, imageBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(imageBytes);
            }
        } catch (NumberFormatException e) {
            exchange.sendResponseHeaders(400, -1);
        }
    }

    private String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    private Optional<Profile> profileServiceFind(Long id) {
        return profileService.findById(id);
    }

    private Integer parseAge(String ageParam) {
        try {
            return Integer.parseInt(ageParam);
        } catch (Exception e) {
            throw new IllegalArgumentException("Age must be a number");
        }
    }

    private Map<String, String> parseFormBody(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return parseQueryString(body);
    }

    private Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isBlank()) {
            return result;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        String fullHtml = "<html><head>" + STYLE + "</head><body>" + html + "</body></html>";
        exchange.getResponseHeaders().put("Content-Type", List.of("text/html"));
        byte[] bytes = fullHtml.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
package com.cqu.cupid.profile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ProfileWebServer {

    private final ProfileService profileService;

    public ProfileWebServer(ProfileService profileService) {
        this.profileService = profileService;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handleHome);
        server.createContext("/create", this::handleCreate);
        server.createContext("/view", this::handleView);
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
                String html = "<h1>Profile</h1>"
                        + "<p>ID: " + p.getId() + "</p>"
                        + "<p>Name: " + escapeHtml(p.getName()) + "</p>"
                        + "<p>Age: " + p.getAge() + "</p>"
                        + "<p>Bio: " + escapeHtml(p.getBio()) + "</p>"
                        + "<a href='/'>Back</a>";
                sendResponse(exchange, 200, html);
            } else {
                sendResponse(exchange, 404, "<h1>Not found</h1><a href='/'>Back</a>");
            }
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, "Invalid id format");
        }
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
        exchange.getResponseHeaders().put("Content-Type", java.util.List.of("text/html"));
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
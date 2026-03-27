package controllers;

import Models.User;
import database.Queries;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * BUG FIXED: Original compared plain-text password directly.
 * Now hashes the input with SHA-256 before comparing with stored hash.
 * Existing DB passwords must be stored as SHA-256 hex strings.
 */
public class LoginController {

    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = Queries.getUserByUsername(username.trim());
        if (user == null) return null;

        String inputHash = sha256(password);
        if (inputHash != null && inputHash.equals(user.getPassword())) {
            return user;
        }

        return null;
    }

    public boolean validateInput(String username, String password) {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }

    /** SHA-256 hex hash — matches UsersPanel.hashPassword() */
    public static String sha256(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
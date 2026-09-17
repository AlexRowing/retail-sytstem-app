package shelfside;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginProcessor {
    private String targetUsername;
    private String targetPassword;

    public LoginProcessor(String targetUsername, String targetPassword) {
        if (targetUsername == null || targetPassword == null) {
            throw new IllegalArgumentException("Username and password cannot be null");
        }
        this.targetUsername = hash(targetUsername);
        this.targetPassword = hash(targetPassword);
    }

    public String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return hash(username).equals(targetUsername) && hash(password).equals(targetPassword);
    }
}

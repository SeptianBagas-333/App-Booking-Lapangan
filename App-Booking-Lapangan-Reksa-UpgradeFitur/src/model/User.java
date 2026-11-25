package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public abstract class User {
    private final String id;
    private final String username;
    private final String passwordHash;
    private final String nama;

    protected User(String id, String username, String passwordHash, String nama) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nama = nama;
    }

    public abstract String getRole();

    public boolean cocokPassword(String passwordPlain) { return passwordHash.equals(hash(passwordPlain)); }

    public static String hash(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getNama() { return nama; }
}

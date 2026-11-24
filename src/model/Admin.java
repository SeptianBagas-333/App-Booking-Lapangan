package model;

public class Admin extends User {
    public Admin(String id, String username, String passwordHash, String nama) {
        super(id, username, passwordHash, nama);
    }
    @Override public String getRole() { return "ADMIN"; }
}

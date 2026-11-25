package service;

import exception.LoginGagalException;
import model.*;
import repository.UserRepository;

public class AuthService {
    private final UserRepository repo;

    public AuthService(UserRepository repo) { this.repo = repo; }

    public User login(String username, String password) throws LoginGagalException {
        User u = repo.findByUsername(username.trim())
                .orElseThrow(() -> new LoginGagalException("Username atau password salah."));
        if (!u.cocokPassword(password)) throw new LoginGagalException("Username atau password salah.");
        return u;
    }

    public Pelanggan registrasi(String username, String password, String nama) throws LoginGagalException {
        username = username.trim();
        nama = nama.trim();
        if (username.length() < 3 || password.length() < 4 || nama.isEmpty())
            throw new LoginGagalException("Username minimal 3 karakter, password minimal 4 karakter, nama wajib diisi.");
        if (username.contains("|") || nama.contains("|"))
            throw new LoginGagalException("Karakter '|' tidak diperbolehkan.");
        if (repo.findByUsername(username).isPresent())
            throw new LoginGagalException("Username sudah dipakai.");
        Pelanggan p = new Pelanggan(repo.nextId(), username, User.hash(password), nama, Member.BIASA);
        repo.tambah(p);
        return p;
    }
}

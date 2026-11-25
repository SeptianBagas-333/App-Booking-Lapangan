package repository;

import model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final Path file;
    private final List<User> data = new ArrayList<>();

    public UserRepository(Path dir) {
        this.file = dir.resolve("users.csv");
        if (Files.exists(file)) {
            for (String[] c : CsvFile.baca(file)) data.add(parse(c));
        } else {
            data.add(new Admin("U001", "admin", User.hash("admin123"), "Administrator"));
            data.add(new Pelanggan("U002", "budi", User.hash("budi123"), "Budi Santoso", Member.GOLD));
            data.add(new Pelanggan("U003", "siti", User.hash("siti123"), "Siti Aminah", Member.BIASA));
            simpan();
        }
    }

    private User parse(String[] c) {
        // id|role|username|hash|nama|member
        if (c[1].equals("ADMIN")) return new Admin(c[0], c[2], c[3], c[4]);
        return new Pelanggan(c[0], c[2], c[3], c[4], Member.valueOf(c[5]));
    }

    public void simpan() {
        List<String> baris = new ArrayList<>();
        for (User u : data) {
            String member = (u instanceof Pelanggan) ? ((Pelanggan) u).getMember().name() : "-";
            baris.add(String.join("|", u.getId(), u.getRole(), u.getUsername(), u.getPasswordHash(), u.getNama(), member));
        }
        CsvFile.tulis(file, baris);
    }

    public List<User> findAll() { return data; }

    public Optional<User> findByUsername(String username) {
        return data.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public Optional<User> findById(String id) {
        return data.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public String nextId() { return String.format("U%03d", data.size() + 1); }

    public void tambah(User u) { data.add(u); simpan(); }

    /** Memperbarui tier member pelanggan dan menyimpannya secara permanen ke users.csv. */
    public void perbaruiMember(Pelanggan pelanggan, Member tierBaru) {
        pelanggan.setMember(tierBaru);
        findById(pelanggan.getId()).ifPresent(u -> {
            if (u instanceof Pelanggan) ((Pelanggan) u).setMember(tierBaru);
        });
        simpan();
    }

    /** Menyelaraskan ulang tier member terbaru dari users.csv ke data in-memory. */
    public void sinkronDariFile() {
        if (!Files.exists(file)) return;
        for (String[] c : CsvFile.baca(file)) {
            if (c.length >= 6 && !"ADMIN".equals(c[1])) {
                findById(c[0]).ifPresent(u -> {
                    if (u instanceof Pelanggan) ((Pelanggan) u).setMember(Member.valueOf(c[5]));
                });
            }
        }
    }
}

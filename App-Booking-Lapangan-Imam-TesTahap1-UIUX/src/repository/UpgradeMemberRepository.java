package repository;

import model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpgradeMemberRepository {
    private final Path file;
    private final List<UpgradeMember> data = new ArrayList<>();

    public UpgradeMemberRepository(Path dir, UserRepository users) {
        this.file = dir.resolve("upgrade_member.csv");
        if (!Files.exists(file)) return;
        for (String[] c : CsvFile.baca(file)) {
            // id|pelangganId|dariTier|keTier|biaya|metode|status|waktuPengajuan|waktuSelesai
            Optional<User> u = users.findById(c[1]);
            if (u.isEmpty() || !(u.get() instanceof Pelanggan)) continue;
            LocalDateTime selesai = c[8].isEmpty() ? null : LocalDateTime.parse(c[8]);
            data.add(new UpgradeMember(c[0], (Pelanggan) u.get(), Member.valueOf(c[2]),
                    Member.valueOf(c[3]), Double.parseDouble(c[4]), c[5],
                    StatusUpgrade.valueOf(c[6]), LocalDateTime.parse(c[7]), selesai));
        }
    }

    public void simpan() {
        List<String> baris = new ArrayList<>();
        for (UpgradeMember u : data) {
            String selesai = u.getWaktuSelesai() == null ? "" : u.getWaktuSelesai().toString();
            baris.add(String.join("|", u.getId(), u.getPelanggan().getId(),
                    u.getDariTier().name(), u.getKeTier().name(),
                    String.valueOf(u.getBiaya()), u.getMetode(), u.getStatus().name(),
                    u.getWaktuPengajuan().toString(), selesai));
        }
        CsvFile.tulis(file, baris);
    }

    public List<UpgradeMember> findAll() { return data; }

    public Optional<UpgradeMember> findById(String id) {
        return data.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public String nextId() { return String.format("UP%04d", data.size() + 1); }

    public void tambah(UpgradeMember u) { data.add(u); simpan(); }
}

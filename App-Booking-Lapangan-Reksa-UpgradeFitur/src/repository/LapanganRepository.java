package repository;

import model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LapanganRepository {
    private final Path file;
    private final List<Lapangan> data = new ArrayList<>();

    public LapanganRepository(Path dir) {
        this.file = dir.resolve("lapangan.csv");
        if (Files.exists(file)) {
            for (String[] c : CsvFile.baca(file)) data.add(buat(JenisLapangan.valueOf(c[1]), c[0], c[2]));
        } else {
            data.add(buat(JenisLapangan.FUTSAL, "FUT1", "Futsal A"));
            data.add(buat(JenisLapangan.FUTSAL, "FUT2", "Futsal B"));
            data.add(buat(JenisLapangan.MINISOCCER, "MIN1", "Minisoccer 1"));
            data.add(buat(JenisLapangan.BASKET, "BAS1", "Basket Indoor"));
            data.add(buat(JenisLapangan.BADMINTON, "BAD1", "Badminton 1"));
            data.add(buat(JenisLapangan.BADMINTON, "BAD2", "Badminton 2"));
            data.add(buat(JenisLapangan.BADMINTON, "BAD3", "Badminton 3"));
            data.add(buat(JenisLapangan.PADEL, "PAD1", "Padel 1"));
            data.add(buat(JenisLapangan.PADEL, "PAD2", "Padel 2"));
            simpan();
        }
    }

    /** Factory: membuat subclass yang sesuai jenisnya. */
    public static Lapangan buat(JenisLapangan j, String id, String nama) {
        switch (j) {
            case FUTSAL:     return new LapanganFutsal(id, nama);
            case MINISOCCER: return new LapanganMinisoccer(id, nama);
            case BASKET:     return new LapanganBasket(id, nama);
            case BADMINTON:  return new LapanganBadminton(id, nama);
            default:         return new LapanganPadel(id, nama);
        }
    }

    private void simpan() {
        List<String> baris = new ArrayList<>();
        for (Lapangan l : data) baris.add(String.join("|", l.getId(), l.getJenis().name(), l.getNama()));
        CsvFile.tulis(file, baris);
    }

    public List<Lapangan> findAll() { return data; }

    public List<Lapangan> findByJenis(JenisLapangan j) {
        List<Lapangan> hasil = new ArrayList<>();
        for (Lapangan l : data) if (l.getJenis() == j) hasil.add(l);
        return hasil;
    }

    public Optional<Lapangan> findById(String id) {
        return data.stream().filter(l -> l.getId().equals(id)).findFirst();
    }
}

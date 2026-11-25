package repository;

import model.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingRepository {
    private final Path file;
    private final List<Booking> data = new ArrayList<>();

    public BookingRepository(Path dir, UserRepository users, LapanganRepository lapangan) {
        this.file = dir.resolve("booking.csv");
        if (!Files.exists(file)) return;
        for (String[] c : CsvFile.baca(file)) {
            // id|pelangganId|lapanganId|mulai|durasi|dibuat|batas|status|metode|subtotal|diskon|total|dibayar|refund
            Optional<User> u = users.findById(c[1]);
            Optional<Lapangan> l = lapangan.findById(c[2]);
            if (u.isEmpty() || l.isEmpty() || !(u.get() instanceof Pelanggan)) continue;
            data.add(new Booking(c[0], (Pelanggan) u.get(), l.get(), LocalDateTime.parse(c[3]),
                    Integer.parseInt(c[4]), LocalDateTime.parse(c[5]), LocalDateTime.parse(c[6]),
                    StatusBooking.valueOf(c[7]), c[8], Double.parseDouble(c[9]), Double.parseDouble(c[10]),
                    Double.parseDouble(c[11]), Double.parseDouble(c[12]), Double.parseDouble(c[13])));
        }
    }

    public void simpan() {
        List<String> baris = new ArrayList<>();
        for (Booking b : data) {
            baris.add(String.join("|", b.getId(), b.getPelanggan().getId(), b.getLapangan().getId(),
                    b.getMulai().toString(), String.valueOf(b.getDurasi()), b.getWaktuDibuat().toString(),
                    b.getBatasBayar().toString(), b.getStatus().name(), b.getMetode(),
                    String.valueOf(b.getSubtotal()), String.valueOf(b.getDiskon()), String.valueOf(b.getTotal()),
                    String.valueOf(b.getDibayar()), String.valueOf(b.getRefund())));
        }
        CsvFile.tulis(file, baris);
    }

    public List<Booking> findAll() { return data; }

    public Optional<Booking> findById(String id) {
        return data.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    public String nextId() { return String.format("BK%04d", data.size() + 1); }

    public void tambah(Booking b) { data.add(b); simpan(); }
}

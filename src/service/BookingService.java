package service;

import exception.*;
import model.*;
import repository.BookingRepository;
import repository.LapanganRepository;
import util.Fmt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BookingService {
    public static final int JAM_BUKA = 8;
    public static final int JAM_TUTUP = 22;
    public static final int MAKS_DURASI = 3;
    public static final int BATAS_BAYAR_MENIT = 30;
    public static final int MINIMAL_SEBELUM_MAIN_MENIT = 60;

    /** Hasil perhitungan biaya. */
    public static class Rincian {
        public final double sewa, diskon, total;
        public final String namaDiskon;
        Rincian(double sewa, double diskon, double total, String namaDiskon) {
            this.sewa = sewa; this.diskon = diskon; this.total = total; this.namaDiskon = namaDiskon;
        }
    }

    private final BookingRepository bookingRepo;
    private final LapanganRepository lapanganRepo;
    private final Path folderStruk;

    public BookingService(BookingRepository bookingRepo, LapanganRepository lapanganRepo, Path folderStruk) {
        this.bookingRepo = bookingRepo;
        this.lapanganRepo = lapanganRepo;
        this.folderStruk = folderStruk;
    }

    // ---------- query ----------
    public List<Lapangan> getLapangan() { return lapanganRepo.findAll(); }
    public List<Lapangan> getLapangan(JenisLapangan j) { return lapanganRepo.findByJenis(j); }
    public List<Booking> getSemuaBooking() { return bookingRepo.findAll(); }

    public List<Booking> getBookingPelanggan(Pelanggan p) {
        return bookingRepo.findAll().stream()
                .filter(b -> b.getPelanggan().getId().equals(p.getId())).collect(Collectors.toList());
    }

    public Optional<Booking> cariById(String id) { return bookingRepo.findById(id); }

    /** Booking aktif (mengunci slot) pada lapangan di jam tertentu, atau null. */
    public Booking cariBookingAktif(Lapangan l, LocalDateTime jam) {
        for (Booking b : bookingRepo.findAll())
            if (b.getStatus().mengunciSlot() && b.bentrok(l, jam, 1)) return b;
        return null;
    }

    public boolean isSlotTersedia(Lapangan l, LocalDateTime mulai, int durasi) {
        for (Booking b : bookingRepo.findAll())
            if (b.getStatus().mengunciSlot() && b.bentrok(l, mulai, durasi)) return false;
        return true;
    }

    // ---------- biaya ----------
    public Rincian hitungRincian(Lapangan l, LocalDateTime mulai, int durasi, Pelanggan p) {
        double sewa = l.hitungBiaya(mulai, durasi);
        Diskon d = p.getDiskon();
        double potongan = d.hitungDiskon(sewa);
        return new Rincian(sewa, potongan, sewa - potongan, d.getNama());
    }

    // ---------- alur booking ----------
    public Booking buatBooking(Pelanggan p, Lapangan l, LocalDateTime mulai, int durasi, LocalDateTime sekarang)
            throws JadwalBentrokException {
        perbaruiStatus(sekarang);
        if (durasi < 1 || durasi > MAKS_DURASI)
            throw new IllegalArgumentException("Durasi booking 1 sampai " + MAKS_DURASI + " jam.");
        if (mulai.getMinute() != 0 || mulai.getHour() < JAM_BUKA || mulai.plusHours(durasi).toLocalDate().isAfter(mulai.toLocalDate())
                || mulai.getHour() + durasi > JAM_TUTUP)
            throw new IllegalArgumentException("Jam operasional " + JAM_BUKA + ":00 - " + JAM_TUTUP + ":00 (jam penuh).");
        if (mulai.isBefore(sekarang.plusMinutes(MINIMAL_SEBELUM_MAIN_MENIT)))
            throw new IllegalArgumentException("Booking minimal " + MINIMAL_SEBELUM_MAIN_MENIT + " menit sebelum waktu main.");
        if (!isSlotTersedia(l, mulai, durasi))
            throw new JadwalBentrokException("Jadwal bentrok dengan booking lain. Pilih jam lain.");

        Rincian r = hitungRincian(l, mulai, durasi, p);
        LocalDateTime batas = sekarang.plusMinutes(BATAS_BAYAR_MENIT);
        if (batas.isAfter(mulai)) batas = mulai;
        Booking b = new Booking(bookingRepo.nextId(), p, l, mulai, durasi, sekarang, batas,
                StatusBooking.MENUNGGU_BAYAR, "-", r.sewa, r.diskon, r.total, 0, 0);
        bookingRepo.tambah(b);
        return b;
    }

    public void bayar(Booking b, Pembayaran metode, LocalDateTime sekarang) throws BatasBayarLewatException {
        perbaruiStatus(sekarang);
        if (b.getStatus() == StatusBooking.DIBATALKAN)
            throw new BatasBayarLewatException("Batas waktu pembayaran sudah lewat, booking dibatalkan otomatis.");
        if (!b.getStatus().bolehDibayar())
            throw new IllegalStateException("Booking berstatus '" + b.getStatus() + "' tidak dapat dibayar.");
        b.setMetode(metode.getNamaMetode());
        if (metode.perluKonfirmasiAdmin()) {
            b.setStatus(StatusBooking.MENUNGGU_KONFIRMASI);
        } else {
            b.setStatus(StatusBooking.LUNAS);
            b.setDibayar(b.getTotal());
        }
        bookingRepo.simpan();
    }

    public void konfirmasi(Booking b, LocalDateTime sekarang) {
        perbaruiStatus(sekarang);
        if (!b.getStatus().bolehDikonfirmasi())
            throw new IllegalStateException("Hanya booking 'Menunggu Konfirmasi' yang dapat dikonfirmasi.");
        b.setStatus(StatusBooking.LUNAS);
        b.setDibayar(b.getTotal());
        bookingRepo.simpan();
    }

    /** Membatalkan booking. Refund hanya jika sudah LUNAS, sesuai kebijakan. Mengembalikan jumlah refund. */
    public double batalkan(Booking b, LocalDateTime sekarang) throws PembatalanTidakValidException {
        perbaruiStatus(sekarang);
        if (!b.getStatus().bolehDibatalkan())
            throw new PembatalanTidakValidException("Booking berstatus '" + b.getStatus() + "' tidak dapat dibatalkan.");
        if (!b.bolehDibatalkan(sekarang))
            throw new PembatalanTidakValidException("Waktu main sudah dimulai, booking tidak dapat dibatalkan.");
        double refund = b.hitungRefund(sekarang);
        b.setRefund(refund);
        b.setStatus(StatusBooking.DIBATALKAN);
        bookingRepo.simpan();
        return refund;
    }

    /** Lazy expiration: dipanggil saat buka aplikasi, cek jadwal, dan oleh Timer. */
    public void perbaruiStatus(LocalDateTime sekarang) {
        boolean berubah = false;
        for (Booking b : bookingRepo.findAll()) {
            if (b.isKedaluwarsa(sekarang)) {
                b.setStatus(StatusBooking.DIBATALKAN);
                berubah = true;
            } else if (b.getStatus() == StatusBooking.MENUNGGU_KONFIRMASI && !sekarang.isBefore(b.getMulai())) {
                b.setStatus(StatusBooking.DIBATALKAN);  // admin tidak konfirmasi sampai waktu main
                berubah = true;
            } else if (b.getStatus() == StatusBooking.LUNAS && !sekarang.isBefore(b.getSelesai())) {
                b.setStatus(StatusBooking.SELESAI);
                berubah = true;
            }
        }
        if (berubah) bookingRepo.simpan();
    }

    // ---------- laporan ----------
    public double pendapatanBersih(Booking b) { return b.getDibayar() - b.getRefund(); }

    public double totalPendapatan() {
        return bookingRepo.findAll().stream().mapToDouble(this::pendapatanBersih).sum();
    }

    public Map<JenisLapangan, Double> pendapatanPerJenis() {
        Map<JenisLapangan, Double> m = new EnumMap<>(JenisLapangan.class);
        for (JenisLapangan j : JenisLapangan.values()) m.put(j, 0.0);
        for (Booking b : bookingRepo.findAll()) m.merge(b.getLapangan().getJenis(), pendapatanBersih(b), Double::sum);
        return m;
    }

    public Map<StatusBooking, Long> jumlahPerStatus() {
        Map<StatusBooking, Long> m = new EnumMap<>(StatusBooking.class);
        for (StatusBooking s : StatusBooking.values()) m.put(s, 0L);
        for (Booking b : bookingRepo.findAll()) m.merge(b.getStatus(), 1L, Long::sum);
        return m;
    }

    // ---------- struk ----------
    public String buatTeksStruk(Booking b) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============================\n");
        sb.append("   STRUK BOOKING LAPANGAN\n");
        sb.append("==============================\n");
        sb.append(String.format("No. Booking : %s%n", b.getId()));
        sb.append(String.format("Pelanggan   : %s%n", b.getPelanggan().getNama()));
        sb.append(String.format("Lapangan    : %s (%s)%n", b.getLapangan().getNama(), b.getLapangan().getJenis()));
        sb.append(String.format("Tanggal     : %s%n", b.getMulai().format(Fmt.TANGGAL)));
        sb.append(String.format("Jam         : %s - %s (%d jam)%n", b.getMulai().format(Fmt.JAM),
                b.getSelesai().format(Fmt.JAM), b.getDurasi()));
        sb.append("------------------------------\n");
        sb.append(String.format("Biaya sewa  : %s%n", Fmt.rp(b.getSubtotal())));
        sb.append(String.format("Diskon      : -%s%n", Fmt.rp(b.getDiskon())));
        sb.append(String.format("TOTAL       : %s%n", Fmt.rp(b.getTotal())));
        sb.append(String.format("Metode      : %s%n", b.getMetode()));
        sb.append(String.format("Status      : %s%n", b.getStatus()));
        if (b.getRefund() > 0) sb.append(String.format("Refund      : %s%n", Fmt.rp(b.getRefund())));
        sb.append("==============================\n");
        sb.append("Terima kasih telah memesan!\n");
        return sb.toString();
    }

    public Path cetakStruk(Booking b) throws IOException {
        if (b.getStatus() != StatusBooking.LUNAS && b.getStatus() != StatusBooking.SELESAI)
            throw new IllegalStateException("Struk hanya tersedia untuk booking yang sudah lunas.");
        Files.createDirectories(folderStruk);
        Path f = folderStruk.resolve("STRUK-" + b.getId() + ".txt");
        Files.writeString(f, buatTeksStruk(b), StandardCharsets.UTF_8);
        return f;
    }
}

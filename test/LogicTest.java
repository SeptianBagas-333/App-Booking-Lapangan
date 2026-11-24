import exception.*;
import model.*;
import repository.*;
import service.*;

import java.nio.file.*;
import java.time.*;
import java.util.Comparator;

/** Pengujian logika bisnis tanpa GUI. Jalankan dengan: java -ea LogicTest */
public class LogicTest {
    static int lulus = 0;
    static void cek(boolean kondisi, String nama) {
        if (!kondisi) throw new AssertionError("GAGAL: " + nama);
        lulus++;
        System.out.println("  [OK] " + nama);
    }
    static void hampir(double a, double b, String nama) { cek(Math.abs(a - b) < 0.01, nama + " (" + a + ")"); }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("bookingtest");
        UserRepository ur = new UserRepository(dir);
        LapanganRepository lr = new LapanganRepository(dir);
        BookingRepository br = new BookingRepository(dir, ur, lr);
        BookingService bs = new BookingService(br, lr, dir.resolve("struk"));
        AuthService auth = new AuthService(ur);

        Pelanggan budi = (Pelanggan) auth.login("budi", "budi123");    // GOLD 10%
        Pelanggan siti = (Pelanggan) auth.login("siti", "siti123");    // BIASA
        Lapangan futsal = lr.findById("FUT1").get();
        Lapangan padel = lr.findById("PAD1").get();

        System.out.println("== Auth");
        cek(auth.login("admin", "admin123") instanceof Admin, "login admin");
        try { auth.login("budi", "salah"); cek(false, "password salah ditolak"); }
        catch (LoginGagalException e) { cek(true, "password salah ditolak"); }
        Pelanggan baru = auth.registrasi("andi", "andi1", "Andi");
        cek(baru.getMember() == Member.BIASA, "registrasi berhasil");
        try { auth.registrasi("andi", "xxxx", "Dobel"); cek(false, "username dobel"); }
        catch (LoginGagalException e) { cek(true, "username dobel ditolak"); }

        System.out.println("== Biaya");
        LocalDate rabu = LocalDate.of(2026, 12, 2);       // hari kerja
        LocalDate sabtu = LocalDate.of(2026, 12, 5);
        hampir(futsal.hitungBiaya(rabu.atTime(10, 0), 1), 120000, "futsal siang weekday");
        hampir(futsal.hitungBiaya(rabu.atTime(18, 0), 1), 150000, "futsal jam sibuk (x1.25)");
        hampir(futsal.hitungBiaya(sabtu.atTime(10, 0), 1), 138000, "futsal weekend (x1.15)");
        hampir(futsal.hitungBiaya(sabtu.atTime(18, 0), 1), 172500, "futsal sibuk + weekend");
        hampir(futsal.hitungBiaya(rabu.atTime(16, 0), 2), 120000 + 150000, "durasi lintas jam sibuk");
        hampir(padel.hitungBiaya(rabu.atTime(10, 0), 1), 230000, "padel + biaya tambahan 30rb");
        hampir(bs.hitungRincian(futsal, rabu.atTime(10, 0), 1, budi).total, 108000, "diskon GOLD 10%");
        hampir(bs.hitungRincian(futsal, rabu.atTime(10, 0), 1, siti).total, 120000, "tanpa diskon");

        System.out.println("== Booking, bentrok, kedaluwarsa");
        LocalDateTime now = rabu.atTime(8, 0);
        Booking b1 = bs.buatBooking(siti, futsal, rabu.atTime(14, 0), 2, now);
        cek(b1.getStatus() == StatusBooking.MENUNGGU_BAYAR, "status awal menunggu bayar");
        cek(b1.getBatasBayar().equals(now.plusMinutes(30)), "batas bayar 30 menit");
        try { bs.buatBooking(budi, futsal, rabu.atTime(15, 0), 1, now); cek(false, "bentrok"); }
        catch (JadwalBentrokException e) { cek(true, "jadwal bentrok ditolak"); }
        bs.buatBooking(budi, lr.findById("FUT2").get(), rabu.atTime(15, 0), 1, now);
        cek(true, "lapangan lain di jam sama boleh");
        try { bs.buatBooking(siti, futsal, rabu.atTime(8, 30), 1, now); cek(false, "menit"); }
        catch (IllegalArgumentException e) { cek(true, "jam tidak penuh ditolak"); }
        try { bs.buatBooking(siti, futsal, rabu.atTime(8, 0), 1, now); cek(false, "terlalu dekat"); }
        catch (IllegalArgumentException e) { cek(true, "kurang dari 1 jam ditolak"); }
        try { bs.buatBooking(siti, futsal, rabu.atTime(21, 0), 2, now); cek(false, "tutup"); }
        catch (IllegalArgumentException e) { cek(true, "melewati jam tutup ditolak"); }

        LocalDateTime lewat = now.plusMinutes(31);
        try { bs.bayar(b1, new PembayaranEWallet(), lewat); cek(false, "bayar telat"); }
        catch (BatasBayarLewatException e) { cek(true, "bayar setelah batas ditolak"); }
        cek(b1.getStatus() == StatusBooking.DIBATALKAN, "otomatis dibatalkan tanpa refund");
        cek(bs.isSlotTersedia(futsal, rabu.atTime(14, 0), 2), "slot dibebaskan kembali");

        System.out.println("== Pembayaran & konfirmasi admin");
        Booking b2 = bs.buatBooking(siti, futsal, rabu.atTime(14, 0), 1, lewat);
        bs.bayar(b2, Pembayaran.dari("E-Wallet"), lewat);
        cek(b2.getStatus() == StatusBooking.LUNAS, "e-wallet langsung lunas");
        Booking b3 = bs.buatBooking(siti, futsal, rabu.atTime(16, 0), 1, lewat);
        bs.bayar(b3, Pembayaran.dari("Transfer"), lewat);
        cek(b3.getStatus() == StatusBooking.MENUNGGU_KONFIRMASI, "transfer menunggu konfirmasi");
        cek(!bs.getSemuaBooking().isEmpty() && b3.getDibayar() == 0, "belum ada uang masuk sebelum konfirmasi");
        bs.perbaruiStatus(lewat.plusHours(2));
        cek(b3.getStatus() == StatusBooking.MENUNGGU_KONFIRMASI, "batas 30 menit tidak berlaku setelah transfer");
        bs.konfirmasi(b3, lewat.plusHours(2));
        cek(b3.getStatus() == StatusBooking.LUNAS && b3.getDibayar() == b3.getTotal(), "admin konfirmasi -> lunas");

        System.out.println("== Refund");
        Booking r1 = lunas(bs, siti, lr.findById("BAS1").get(), LocalDate.of(2026, 12, 10).atTime(10, 0), now);
        hampir(r1.hitungRefund(LocalDate.of(2026, 12, 8).atTime(10, 0)), r1.getTotal(), "H-2: refund 100%");
        hampir(r1.hitungRefund(LocalDate.of(2026, 12, 9).atTime(10, 0)), r1.getTotal(), "tepat 24 jam: 100%");
        hampir(r1.hitungRefund(LocalDate.of(2026, 12, 9).atTime(12, 0)), r1.getTotal() / 2, "22 jam: 50%");
        hampir(r1.hitungRefund(LocalDate.of(2026, 12, 10).atTime(7, 0)), r1.getTotal() / 2, "tepat 3 jam: 50%");
        hampir(r1.hitungRefund(LocalDate.of(2026, 12, 10).atTime(8, 0)), 0, "2 jam: 0%");
        double refund = bs.batalkan(r1, LocalDate.of(2026, 12, 9).atTime(12, 0));
        hampir(refund, r1.getTotal() / 2, "batalkan lunas -> refund tersimpan");
        cek(r1.getStatus() == StatusBooking.DIBATALKAN, "status dibatalkan");
        try { bs.batalkan(r1, now); cek(false, "batal dua kali"); }
        catch (PembatalanTidakValidException e) { cek(true, "batal ulang ditolak"); }

        Booking pending = bs.buatBooking(siti, padel, LocalDate.of(2026, 12, 11).atTime(10, 0), 1, now);
        hampir(bs.batalkan(pending, now), 0, "batal sebelum bayar tanpa refund");

        System.out.println("== Selesai, laporan, struk");
        bs.perbaruiStatus(rabu.atTime(23, 0));
        cek(b2.getStatus() == StatusBooking.SELESAI, "lunas otomatis selesai setelah main");
        double expected = b2.getTotal() + b3.getTotal() + r1.getTotal() / 2;
        hampir(bs.totalPendapatan(), expected, "pendapatan bersih = lunas + sisa refund");
        Path struk = bs.cetakStruk(b2);
        cek(Files.readString(struk).contains("TOTAL"), "struk tercetak ke file");
        try { bs.cetakStruk(pending); cek(false, "struk"); }
        catch (IllegalStateException e) { cek(true, "struk ditolak untuk booking tidak lunas"); }

        System.out.println("== Persistensi CSV");
        BookingRepository br2 = new BookingRepository(dir, new UserRepository(dir), new LapanganRepository(dir));
        cek(br2.findAll().size() == br.findAll().size(), "jumlah booking sama setelah dimuat ulang");
        Booking muat = br2.findById(r1.getId()).get();
        cek(muat.getStatus() == StatusBooking.DIBATALKAN && Math.abs(muat.getRefund() - r1.getRefund()) < 0.01,
                "status dan refund terbaca benar dari CSV");

        System.out.println("\nSEMUA " + lulus + " PENGUJIAN LULUS");
        try (var s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }

    static Booking lunas(BookingService bs, Pelanggan p, Lapangan l, LocalDateTime mulai, LocalDateTime now) throws Exception {
        Booking b = bs.buatBooking(p, l, mulai, 1, now);
        bs.bayar(b, Pembayaran.dari("E-Wallet"), now);
        return b;
    }
}

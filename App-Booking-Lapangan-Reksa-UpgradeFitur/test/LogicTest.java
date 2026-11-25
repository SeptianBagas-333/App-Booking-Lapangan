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

        System.out.println("== Upgrade Member Berbayar");
        UpgradeMemberRepository upr = new UpgradeMemberRepository(dir, ur);
        UpgradeMemberService us = new UpgradeMemberService(upr, ur);

        // 1. Validasi tarif baru & tier tidak valid
        hampir(us.hitungBiaya(Member.BIASA, Member.SILVER), 50000, "biaya BIASA -> SILVER 50rb");
        hampir(us.hitungBiaya(Member.BIASA, Member.GOLD), 75000, "biaya BIASA -> GOLD 75rb");
        hampir(us.hitungBiaya(Member.SILVER, Member.GOLD), 30000, "biaya SILVER -> GOLD 30rb");
        try { us.ajukanUpgrade(siti, Member.BIASA, Pembayaran.dari("E-Wallet"), now); cek(false, "tier sama"); }
        catch (IllegalArgumentException e) { cek(true, "upgrade ke tier sama ditolak"); }
        try { us.ajukanUpgrade(budi, Member.SILVER, Pembayaran.dari("E-Wallet"), now); cek(false, "downgrade"); }
        catch (IllegalArgumentException e) { cek(true, "downgrade GOLD -> SILVER ditolak"); }
        try { us.ajukanUpgrade(budi, Member.GOLD, Pembayaran.dari("E-Wallet"), now); cek(false, "gold ke gold"); }
        catch (IllegalArgumentException e) { cek(true, "GOLD tidak dapat upgrade lagi"); }

        // 2. Upgrade via E-Wallet (BIASA -> SILVER langsung LUNAS & diskon 5% aktif)
        UpgradeMember u1 = us.ajukanUpgrade(siti, Member.SILVER, Pembayaran.dari("E-Wallet"), now);
        cek(u1.getStatus() == StatusUpgrade.LUNAS, "e-wallet upgrade langsung lunas");
        cek(siti.getMember() == Member.SILVER, "tier siti langsung berubah ke SILVER");
        hampir(u1.getBiaya(), 50000, "biaya u1 tercatat 50rb");
        hampir(bs.hitungRincian(futsal, rabu.atTime(10, 0), 1, siti).total, 114000, "diskon SILVER 5% langsung berlaku");

        // 3. Upgrade via Transfer + validasi pengajuan ganda + konfirmasi admin (SILVER -> GOLD Rp30.000)
        UpgradeMember u2 = us.ajukanUpgrade(siti, Member.GOLD, Pembayaran.dari("Transfer"), now.plusMinutes(5));
        cek(u2.getStatus() == StatusUpgrade.MENUNGGU_KONFIRMASI, "transfer upgrade menunggu konfirmasi");
        cek(siti.getMember() == Member.SILVER, "tier belum berubah sebelum konfirmasi admin");
        hampir(u2.getBiaya(), 30000, "biaya u2 (SILVER -> GOLD) tercatat 30rb");
        try { us.ajukanUpgrade(siti, Member.GOLD, Pembayaran.dari("E-Wallet"), now.plusMinutes(6)); cek(false, "pengajuan ganda"); }
        catch (IllegalStateException e) { cek(true, "pengajuan ganda saat menunggu konfirmasi ditolak"); }
        us.konfirmasi(u2, now.plusMinutes(10));
        cek(u2.getStatus() == StatusUpgrade.LUNAS, "admin konfirmasi upgrade -> LUNAS");
        cek(siti.getMember() == Member.GOLD, "tier siti naik ke GOLD setelah konfirmasi");
        hampir(bs.hitungRincian(futsal, rabu.atTime(10, 0), 1, siti).total, 108000, "diskon GOLD 10% langsung berlaku");
        try { us.konfirmasi(u2, now.plusMinutes(11)); cek(false, "konfirmasi ulang"); }
        catch (IllegalStateException e) { cek(true, "konfirmasi ulang ditolak"); }

        // 4. Upgrade via Tunai + penolakan admin + pengajuan ulang (BIASA -> GOLD Rp75.000 untuk user 'baru')
        UpgradeMember u3 = us.ajukanUpgrade(baru, Member.GOLD, Pembayaran.dari("Tunai"), now.plusMinutes(15));
        cek(u3.getStatus() == StatusUpgrade.MENUNGGU_KONFIRMASI, "tunai upgrade menunggu konfirmasi");
        cek(baru.getMember() == Member.BIASA, "tier baru masih BIASA sebelum konfirmasi");
        hampir(u3.getBiaya(), 75000, "biaya u3 (BIASA -> GOLD) tercatat 75rb");
        us.batalkan(u3, now.plusMinutes(20));
        cek(u3.getStatus() == StatusUpgrade.DIBATALKAN, "admin batalkan pengajuan -> DIBATALKAN");
        cek(baru.getMember() == Member.BIASA, "tier baru tetap BIASA setelah ditolak");
        try { us.batalkan(u3, now.plusMinutes(21)); cek(false, "batal ulang"); }
        catch (IllegalStateException e) { cek(true, "pembatalan ulang ditolak"); }
        // Setelah dibatalkan, boleh mengajukan lagi
        UpgradeMember u4 = us.ajukanUpgrade(baru, Member.GOLD, Pembayaran.dari("Tunai"), now.plusMinutes(25));
        us.konfirmasi(u4, now.plusMinutes(30));
        cek(u4.getStatus() == StatusUpgrade.LUNAS && baru.getMember() == Member.GOLD, "pengajuan ulang & konfirmasi berhasil");

        // 5. Laporan pendapatan upgrade & gabungan dengan pendapatan booking (50rb + 30rb + 75rb = 155rb)
        hampir(us.totalPendapatan(), 50000 + 30000 + 75000, "pendapatan upgrade hanya hitung LUNAS (155rb)");
        cek(us.jumlahPerStatus().get(StatusUpgrade.LUNAS) == 3L, "jumlah upgrade LUNAS = 3");
        cek(us.jumlahPerStatus().get(StatusUpgrade.DIBATALKAN) == 1L, "jumlah upgrade DIBATALKAN = 1");
        cek(us.jumlahPerStatus().get(StatusUpgrade.MENUNGGU_KONFIRMASI) == 0L, "jumlah upgrade MENUNGGU_KONFIRMASI = 0");
        hampir(bs.totalPendapatan() + us.totalPendapatan(), expected + 155000, "total pendapatan bersih gabungan akurat");

        // 6. Batalkan & Downgrade (Revert Transaksi LUNAS)
        // siti punya u1 (BIASA->SILVER, LUNAS) lalu u2 (SILVER->GOLD, LUNAS); u1 tidak boleh di-revert sebelum u2
        try { us.batalkanDanDowngrade(u1, now.plusMinutes(35)); cek(false, "revert u1 lama ditolak"); }
        catch (IllegalStateException e) { cek(true, "revert transaksi LUNAS lama (BIASA->SILVER) saat sudah GOLD ditolak"); }
        // Transaksi DIBATALKAN tidak boleh di-revert
        try { us.batalkanDanDowngrade(u3, now.plusMinutes(35)); cek(false, "revert DIBATALKAN ditolak"); }
        catch (IllegalStateException e) { cek(true, "revert transaksi DIBATALKAN ditolak"); }
        // Revert u2 (SILVER->GOLD) berhasil -> status DIBATALKAN, tier siti kembali ke SILVER, pendapatan berkurang 30rb
        us.batalkanDanDowngrade(u2, now.plusMinutes(40));
        cek(u2.getStatus() == StatusUpgrade.DIBATALKAN, "batalkanDanDowngrade u2 -> status DIBATALKAN");
        cek(siti.getMember() == Member.SILVER, "tier siti turun kembali ke SILVER setelah revert u2");
        hampir(bs.hitungRincian(futsal, rabu.atTime(10, 0), 1, siti).total, 114000, "diskon siti kembali ke SILVER 5%");
        hampir(us.totalPendapatan(), 50000 + 75000, "pendapatan upgrade berkurang 30rb setelah revert u2 (125rb)");
        hampir(bs.totalPendapatan() + us.totalPendapatan(), expected + 125000, "total pendapatan gabungan berkurang 30rb");
        // Setelah u2 di-revert, u1 (BIASA->SILVER) kini transaksi LUNAS terbaru -> boleh di-revert kembali ke BIASA
        us.batalkanDanDowngrade(u1, now.plusMinutes(45));
        cek(u1.getStatus() == StatusUpgrade.DIBATALKAN, "batalkanDanDowngrade u1 setelah u2 di-revert -> DIBATALKAN");
        cek(siti.getMember() == Member.BIASA, "tier siti turun kembali ke BIASA setelah revert u1");
        hampir(bs.hitungRincian(futsal, rabu.atTime(10, 0), 1, siti).total, 120000, "diskon siti kembali tanpa diskon");
        hampir(us.totalPendapatan(), 75000, "pendapatan upgrade tersisa 75rb (hanya u4 yang masih LUNAS)");
        cek(us.jumlahPerStatus().get(StatusUpgrade.LUNAS) == 1L, "jumlah upgrade LUNAS menjadi 1");
        cek(us.jumlahPerStatus().get(StatusUpgrade.DIBATALKAN) == 3L, "jumlah upgrade DIBATALKAN menjadi 3");

        // 7. Persistensi upgrade_member.csv dan users.csv (termasuk hasil downgrade)
        UserRepository ur2 = new UserRepository(dir);
        UpgradeMemberRepository upr2 = new UpgradeMemberRepository(dir, ur2);
        Pelanggan sitiMuat = (Pelanggan) ur2.findByUsername("siti").get();
        Pelanggan baruMuat = (Pelanggan) ur2.findByUsername("andi").get();
        cek(sitiMuat.getMember() == Member.BIASA, "tier BIASA siti (hasil downgrade) tersimpan permanen di users.csv");
        cek(baruMuat.getMember() == Member.GOLD, "tier GOLD andi tersimpan permanen di users.csv");
        cek(upr2.findAll().size() == 4, "seluruh 4 transaksi upgrade terbaca dari upgrade_member.csv");
        UpgradeMember u2Muat = upr2.findById(u2.getId()).get();
        cek(u2Muat.getStatus() == StatusUpgrade.DIBATALKAN && u2Muat.getDariTier() == Member.SILVER
                && u2Muat.getKeTier() == Member.GOLD, "detail transaksi revert terbaca benar dari CSV");
        UpgradeMemberService us2 = new UpgradeMemberService(upr2, ur2);
        hampir(us2.totalPendapatan(), 75000, "total pendapatan upgrade konsisten setelah muat ulang CSV");

        // Sinkronisasi ulang Pelanggan aktif dari users.csv saat admin downgrade dari instance terpisah
        UpgradeMember u4Muat = upr2.findById(u4.getId()).get();
        us2.batalkanDanDowngrade(u4Muat, now.plusMinutes(50));
        us.segarkanPelanggan(baru);
        cek(baru.getMember() == Member.BIASA, "segarkanPelanggan menyinkronkan downgrade terbaru dari users.csv");

        System.out.println("\nSEMUA " + lulus + " PENGUJIAN LULUS");
        try (var s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }

    static Booking lunas(BookingService bs, Pelanggan p, Lapangan l, LocalDateTime mulai, LocalDateTime now) throws Exception {
        Booking b = bs.buatBooking(p, l, mulai, 1, now);
        bs.bayar(b, Pembayaran.dari("E-Wallet"), now);
        return b;
    }
}

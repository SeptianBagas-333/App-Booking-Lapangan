package service;

import model.*;
import repository.UpgradeMemberRepository;
import repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class UpgradeMemberService {
    public static final double BIAYA_BIASA_KE_SILVER = 50_000;
    public static final double BIAYA_BIASA_KE_GOLD = 75_000;
    public static final double BIAYA_SILVER_KE_GOLD = 30_000;

    private final UpgradeMemberRepository upgradeRepo;
    private final UserRepository userRepo;

    public UpgradeMemberService(UpgradeMemberRepository upgradeRepo, UserRepository userRepo) {
        this.upgradeRepo = upgradeRepo;
        this.userRepo = userRepo;
    }

    // ---------- tarif & aturan tier ----------
    public double hitungBiaya(Member dari, Member ke) {
        if (dari == null || ke == null)
            throw new IllegalArgumentException("Tier member tidak boleh kosong.");
        if (dari == Member.BIASA && ke == Member.SILVER) return BIAYA_BIASA_KE_SILVER;
        if (dari == Member.SILVER && ke == Member.GOLD) return BIAYA_SILVER_KE_GOLD;
        if (dari == Member.BIASA && ke == Member.GOLD) return BIAYA_BIASA_KE_GOLD;
        throw new IllegalArgumentException("Upgrade hanya diperbolehkan ke tier member yang lebih tinggi.");
    }

    public List<Member> getTargetUpgradeTersedia(Member saatIni) {
        if (saatIni == Member.BIASA) return Arrays.asList(Member.SILVER, Member.GOLD);
        if (saatIni == Member.SILVER) return Collections.singletonList(Member.GOLD);
        return Collections.emptyList();
    }

    public boolean adaPengajuanMenunggu(Pelanggan p) {
        return upgradeRepo.findAll().stream()
                .anyMatch(u -> u.getPelanggan().getId().equals(p.getId())
                        && u.getStatus() == StatusUpgrade.MENUNGGU_KONFIRMASI);
    }

    /** Menyelaraskan tier member pada objek Pelanggan aktif dengan data terbaru di UserRepository. */
    public void segarkanPelanggan(Pelanggan p) {
        userRepo.sinkronDariFile();
        userRepo.findById(p.getId()).ifPresent(u -> {
            if (u instanceof Pelanggan) p.setMember(((Pelanggan) u).getMember());
        });
    }

    // ---------- query ----------
    public List<UpgradeMember> getSemuaPengajuan() { return upgradeRepo.findAll(); }

    public List<UpgradeMember> getPengajuanPelanggan(Pelanggan p) {
        return upgradeRepo.findAll().stream()
                .filter(u -> u.getPelanggan().getId().equals(p.getId()))
                .collect(Collectors.toList());
    }

    public Optional<UpgradeMember> cariById(String id) { return upgradeRepo.findById(id); }

    // ---------- alur upgrade ----------
    public UpgradeMember ajukanUpgrade(Pelanggan p, Member targetTier, Pembayaran metode, LocalDateTime sekarang) {
        if (p == null || metode == null)
            throw new IllegalArgumentException("Data pelanggan dan metode pembayaran wajib diisi.");
        segarkanPelanggan(p);
        Member dariTier = p.getMember();
        double biaya = hitungBiaya(dariTier, targetTier);
        if (adaPengajuanMenunggu(p))
            throw new IllegalStateException("Anda masih memiliki pengajuan upgrade yang menunggu konfirmasi admin.");

        boolean langsungLunas = !metode.perluKonfirmasiAdmin();
        StatusUpgrade status = langsungLunas ? StatusUpgrade.LUNAS : StatusUpgrade.MENUNGGU_KONFIRMASI;
        LocalDateTime selesai = langsungLunas ? sekarang : null;

        UpgradeMember u = new UpgradeMember(upgradeRepo.nextId(), p, dariTier, targetTier,
                biaya, metode.getNamaMetode(), status, sekarang, selesai);
        upgradeRepo.tambah(u);
        if (langsungLunas) {
            userRepo.perbaruiMember(p, targetTier);
        }
        return u;
    }

    public void konfirmasi(UpgradeMember u, LocalDateTime sekarang) {
        if (u == null) throw new IllegalArgumentException("Pengajuan upgrade tidak ditemukan.");
        if (!u.getStatus().bolehDikonfirmasi())
            throw new IllegalStateException("Hanya pengajuan 'Menunggu Konfirmasi' yang dapat dikonfirmasi.");
        u.setStatus(StatusUpgrade.LUNAS);
        u.setWaktuSelesai(sekarang);
        upgradeRepo.simpan();
        userRepo.perbaruiMember(u.getPelanggan(), u.getKeTier());
    }

    public void batalkan(UpgradeMember u, LocalDateTime sekarang) {
        if (u == null) throw new IllegalArgumentException("Pengajuan upgrade tidak ditemukan.");
        if (!u.getStatus().bolehDibatalkan())
            throw new IllegalStateException("Hanya pengajuan 'Menunggu Konfirmasi' yang dapat dibatalkan.");
        u.setStatus(StatusUpgrade.DIBATALKAN);
        u.setWaktuSelesai(sekarang);
        upgradeRepo.simpan();
    }

    /** Memvalidasi apakah transaksi LUNAS boleh dibatalkan dan di-downgrade. */
    public void validasiBatalkanDanDowngrade(UpgradeMember u) {
        if (u == null) throw new IllegalArgumentException("Transaksi upgrade tidak ditemukan.");
        if (!u.getStatus().bolehDiRevert())
            throw new IllegalStateException("Hanya transaksi berstatus 'Lunas' yang dapat dibatalkan dan di-downgrade.");
        Pelanggan p = u.getPelanggan();
        segarkanPelanggan(p);
        if (p.getMember() != u.getKeTier())
            throw new IllegalStateException("Tidak dapat membatalkan transaksi ini karena tier member pelanggan saat ini ("
                    + p.getMember() + ") tidak sama dengan tier tujuan transaksi (" + u.getKeTier() + ").");
        UpgradeMember lunasTerbaru = null;
        for (UpgradeMember item : upgradeRepo.findAll()) {
            if (item.getPelanggan().getId().equals(p.getId()) && item.getStatus() == StatusUpgrade.LUNAS) {
                lunasTerbaru = item;
            }
        }
        if (lunasTerbaru == null || !lunasTerbaru.getId().equals(u.getId()))
            throw new IllegalStateException("Hanya transaksi LUNAS terbaru milik pelanggan yang dapat dibatalkan dan di-downgrade.");
        if (adaPengajuanMenunggu(p))
            throw new IllegalStateException("Pelanggan masih memiliki pengajuan upgrade yang menunggu konfirmasi. Batalkan pengajuan tersebut terlebih dahulu.");
    }

    /** Membatalkan transaksi upgrade LUNAS dan menurunkan kembali tier member pelanggan ke tier asal. */
    public void batalkanDanDowngrade(UpgradeMember u, LocalDateTime sekarang) {
        validasiBatalkanDanDowngrade(u);
        u.setStatus(StatusUpgrade.DIBATALKAN);
        u.setWaktuSelesai(sekarang);
        upgradeRepo.simpan();
        userRepo.perbaruiMember(u.getPelanggan(), u.getDariTier());
    }

    // ---------- laporan ----------
    public double totalPendapatan() {
        return upgradeRepo.findAll().stream()
                .filter(u -> u.getStatus() == StatusUpgrade.LUNAS)
                .mapToDouble(UpgradeMember::getBiaya)
                .sum();
    }

    public Map<StatusUpgrade, Long> jumlahPerStatus() {
        Map<StatusUpgrade, Long> m = new EnumMap<>(StatusUpgrade.class);
        for (StatusUpgrade s : StatusUpgrade.values()) m.put(s, 0L);
        for (UpgradeMember u : upgradeRepo.findAll()) m.merge(u.getStatus(), 1L, Long::sum);
        return m;
    }
}

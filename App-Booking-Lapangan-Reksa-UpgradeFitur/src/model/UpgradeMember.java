package model;

import java.time.LocalDateTime;

/** Entitas pengajuan upgrade member berbayar. */
public class UpgradeMember {
    private final String id;
    private final Pelanggan pelanggan;
    private final Member dariTier;
    private final Member keTier;
    private final double biaya;
    private final String metode;
    private StatusUpgrade status;
    private final LocalDateTime waktuPengajuan;
    private LocalDateTime waktuSelesai;

    public UpgradeMember(String id, Pelanggan pelanggan, Member dariTier, Member keTier,
                         double biaya, String metode, StatusUpgrade status,
                         LocalDateTime waktuPengajuan, LocalDateTime waktuSelesai) {
        this.id = id;
        this.pelanggan = pelanggan;
        this.dariTier = dariTier;
        this.keTier = keTier;
        this.biaya = biaya;
        this.metode = metode;
        this.status = status;
        this.waktuPengajuan = waktuPengajuan;
        this.waktuSelesai = waktuSelesai;
    }

    public String getId() { return id; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public Member getDariTier() { return dariTier; }
    public Member getKeTier() { return keTier; }
    public double getBiaya() { return biaya; }
    public String getMetode() { return metode; }
    public StatusUpgrade getStatus() { return status; }
    public void setStatus(StatusUpgrade status) { this.status = status; }
    public LocalDateTime getWaktuPengajuan() { return waktuPengajuan; }
    public LocalDateTime getWaktuSelesai() { return waktuSelesai; }
    public void setWaktuSelesai(LocalDateTime waktuSelesai) { this.waktuSelesai = waktuSelesai; }
}

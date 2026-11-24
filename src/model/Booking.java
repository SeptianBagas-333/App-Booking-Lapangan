package model;

import java.time.Duration;
import java.time.LocalDateTime;

/** Entitas booking. Menggunakan composition ke Pelanggan dan Lapangan. */
public class Booking implements Cancelable {
    private final String id;
    private final Pelanggan pelanggan;
    private final Lapangan lapangan;
    private final LocalDateTime mulai;
    private final int durasi;
    private final LocalDateTime waktuDibuat;
    private LocalDateTime batasBayar;
    private StatusBooking status;
    private String metode = "-";
    private final double subtotal;
    private final double diskon;
    private final double total;
    private double dibayar;   // uang yang sudah diterima
    private double refund;    // uang yang dikembalikan

    public Booking(String id, Pelanggan pelanggan, Lapangan lapangan, LocalDateTime mulai, int durasi,
                   LocalDateTime waktuDibuat, LocalDateTime batasBayar, StatusBooking status,
                   String metode, double subtotal, double diskon, double total,
                   double dibayar, double refund) {
        this.id = id;
        this.pelanggan = pelanggan;
        this.lapangan = lapangan;
        this.mulai = mulai;
        this.durasi = durasi;
        this.waktuDibuat = waktuDibuat;
        this.batasBayar = batasBayar;
        this.status = status;
        this.metode = metode;
        this.subtotal = subtotal;
        this.diskon = diskon;
        this.total = total;
        this.dibayar = dibayar;
        this.refund = refund;
    }

    public LocalDateTime getSelesai() { return mulai.plusHours(durasi); }

    public boolean isKedaluwarsa(LocalDateTime sekarang) {
        return status == StatusBooking.MENUNGGU_BAYAR && sekarang.isAfter(batasBayar);
    }

    /** Apakah rentang waktu ini bertabrakan dengan booking ini (lapangan sama)? */
    public boolean bentrok(Lapangan l, LocalDateTime m, int d) {
        return lapangan.getId().equals(l.getId())
                && m.isBefore(getSelesai()) && mulai.isBefore(m.plusHours(d));
    }

    @Override public boolean bolehDibatalkan(LocalDateTime sekarang) {
        return status.bolehDibatalkan() && sekarang.isBefore(mulai);
    }

    /** Kebijakan refund (hanya untuk LUNAS): >=24 jam 100%, 3-24 jam 50%, <3 jam 0%. */
    @Override public double hitungRefund(LocalDateTime sekarang) {
        if (status != StatusBooking.LUNAS) return 0;
        long menit = Duration.between(sekarang, mulai).toMinutes();
        if (menit >= 24 * 60) return dibayar;
        if (menit >= 3 * 60) return dibayar * 0.5;
        return 0;
    }

    public String getId() { return id; }
    public Pelanggan getPelanggan() { return pelanggan; }
    public Lapangan getLapangan() { return lapangan; }
    public LocalDateTime getMulai() { return mulai; }
    public int getDurasi() { return durasi; }
    public LocalDateTime getWaktuDibuat() { return waktuDibuat; }
    public LocalDateTime getBatasBayar() { return batasBayar; }
    public StatusBooking getStatus() { return status; }
    public void setStatus(StatusBooking status) { this.status = status; }
    public String getMetode() { return metode; }
    public void setMetode(String metode) { this.metode = metode; }
    public double getSubtotal() { return subtotal; }
    public double getDiskon() { return diskon; }
    public double getTotal() { return total; }
    public double getDibayar() { return dibayar; }
    public void setDibayar(double dibayar) { this.dibayar = dibayar; }
    public double getRefund() { return refund; }
    public void setRefund(double refund) { this.refund = refund; }
}

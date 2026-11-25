package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Kelas abstrak lapangan. Subclass menentukan tarif dasar dan biaya tambahan (polymorphism). */
public abstract class Lapangan {
    public static final double PENGALI_JAM_SIBUK = 1.25;   // 17.00 - 22.00
    public static final double PENGALI_AKHIR_PEKAN = 1.15; // Sabtu & Minggu

    private final String id;
    private final String nama;

    protected Lapangan(String id, String nama) {
        this.id = id;
        this.nama = nama;
    }

    public abstract JenisLapangan getJenis();
    public abstract double getTarifDasar();             // per jam
    public double getBiayaTambahan() { return 0; }      // per booking (lampu, raket, dll)
    public String getKeterangan() { return ""; }

    public static boolean isJamSibuk(int jam) { return jam >= 17 && jam < 22; }
    public static boolean isAkhirPekan(LocalDate d) {
        return d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /** Biaya sewa sebelum diskon: tarif per jam (dengan pengali) + biaya tambahan. */
    public double hitungBiaya(LocalDateTime mulai, int durasiJam) {
        double total = 0;
        for (int i = 0; i < durasiJam; i++) {
            double tarif = getTarifDasar();
            if (isJamSibuk(mulai.plusHours(i).getHour())) tarif *= PENGALI_JAM_SIBUK;
            if (isAkhirPekan(mulai.toLocalDate())) tarif *= PENGALI_AKHIR_PEKAN;
            total += tarif;
        }
        return total + getBiayaTambahan();
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    @Override public String toString() { return nama; }
}

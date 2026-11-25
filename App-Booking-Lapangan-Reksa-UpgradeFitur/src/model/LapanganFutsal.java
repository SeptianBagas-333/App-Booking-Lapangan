package model;

public class LapanganFutsal extends Lapangan {
    public LapanganFutsal(String id, String nama) { super(id, nama); }
    @Override public JenisLapangan getJenis() { return JenisLapangan.FUTSAL; }
    @Override public double getTarifDasar() { return 120000; }
    @Override public double getBiayaTambahan() { return 0; }
    @Override public String getKeterangan() { return "Rumput sintetis"; }
}

package model;

public class LapanganPadel extends Lapangan {
    public LapanganPadel(String id, String nama) { super(id, nama); }
    @Override public JenisLapangan getJenis() { return JenisLapangan.PADEL; }
    @Override public double getTarifDasar() { return 200000; }
    @Override public double getBiayaTambahan() { return 30000; }
    @Override public String getKeterangan() { return "Biaya tambahan: sewa raket dan bola"; }
}

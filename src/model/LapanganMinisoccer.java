package model;

public class LapanganMinisoccer extends Lapangan {
    public LapanganMinisoccer(String id, String nama) { super(id, nama); }
    @Override public JenisLapangan getJenis() { return JenisLapangan.MINISOCCER; }
    @Override public double getTarifDasar() { return 350000; }
    @Override public double getBiayaTambahan() { return 25000; }
    @Override public String getKeterangan() { return "Biaya tambahan: lampu dan wasit"; }
}

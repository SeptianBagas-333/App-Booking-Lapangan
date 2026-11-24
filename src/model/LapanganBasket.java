package model;

public class LapanganBasket extends Lapangan {
    public LapanganBasket(String id, String nama) { super(id, nama); }
    @Override public JenisLapangan getJenis() { return JenisLapangan.BASKET; }
    @Override public double getTarifDasar() { return 150000; }
    @Override public double getBiayaTambahan() { return 0; }
    @Override public String getKeterangan() { return "Lantai indoor"; }
}

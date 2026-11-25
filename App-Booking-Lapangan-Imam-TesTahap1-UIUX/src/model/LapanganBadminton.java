package model;

public class LapanganBadminton extends Lapangan {
    public LapanganBadminton(String id, String nama) { super(id, nama); }
    @Override public JenisLapangan getJenis() { return JenisLapangan.BADMINTON; }
    @Override public double getTarifDasar() { return 50000; }
    @Override public double getBiayaTambahan() { return 10000; }
    @Override public String getKeterangan() { return "Biaya tambahan: shuttlecock"; }
}

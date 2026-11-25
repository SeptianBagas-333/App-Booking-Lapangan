package model;

public class PembayaranTunai implements Pembayaran {
    @Override public boolean perluKonfirmasiAdmin() { return true; }
    @Override public String getNamaMetode() { return "Tunai"; }
}

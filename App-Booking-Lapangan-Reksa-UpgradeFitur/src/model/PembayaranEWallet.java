package model;

public class PembayaranEWallet implements Pembayaran {
    @Override public boolean perluKonfirmasiAdmin() { return false; }
    @Override public String getNamaMetode() { return "E-Wallet"; }
}

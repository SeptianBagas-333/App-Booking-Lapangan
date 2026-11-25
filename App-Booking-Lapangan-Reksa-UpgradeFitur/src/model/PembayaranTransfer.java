package model;

public class PembayaranTransfer implements Pembayaran {
    @Override public boolean perluKonfirmasiAdmin() { return true; }
    @Override public String getNamaMetode() { return "Transfer"; }
}

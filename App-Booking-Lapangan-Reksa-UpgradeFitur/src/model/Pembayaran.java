package model;

public interface Pembayaran {
    boolean perluKonfirmasiAdmin();
    String getNamaMetode();

    String[] DAFTAR_METODE = {"E-Wallet", "Transfer", "Tunai"};

    static Pembayaran dari(String nama) {
        switch (nama) {
            case "E-Wallet": return new PembayaranEWallet();
            case "Transfer": return new PembayaranTransfer();
            case "Tunai":    return new PembayaranTunai();
            default: throw new IllegalArgumentException("Metode pembayaran tidak dikenal: " + nama);
        }
    }
}

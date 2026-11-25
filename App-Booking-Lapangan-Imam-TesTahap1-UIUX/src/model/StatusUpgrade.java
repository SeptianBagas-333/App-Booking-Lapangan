package model;

/** Status pengajuan upgrade member beserta aturan transisinya. */
public enum StatusUpgrade {
    MENUNGGU_KONFIRMASI("Menunggu Konfirmasi"),
    LUNAS("Lunas"),
    DIBATALKAN("Dibatalkan");

    private final String label;

    StatusUpgrade(String label) { this.label = label; }

    public boolean bolehDikonfirmasi() { return this == MENUNGGU_KONFIRMASI; }
    public boolean bolehDibatalkan()   { return this == MENUNGGU_KONFIRMASI; }
    public boolean bolehDiRevert()     { return this == LUNAS; }

    @Override public String toString() { return label; }
}

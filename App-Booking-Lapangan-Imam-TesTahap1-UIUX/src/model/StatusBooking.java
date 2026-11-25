package model;

/** Status booking beserta aturan transisinya (validasi tidak tersebar di GUI). */
public enum StatusBooking {
    MENUNGGU_BAYAR("Menunggu Bayar"),
    MENUNGGU_KONFIRMASI("Menunggu Konfirmasi"),
    LUNAS("Lunas"),
    DIBATALKAN("Dibatalkan"),
    SELESAI("Selesai");

    private final String label;
    StatusBooking(String label) { this.label = label; }

    public boolean bolehDibayar()      { return this == MENUNGGU_BAYAR; }
    public boolean bolehDikonfirmasi() { return this == MENUNGGU_KONFIRMASI; }
    public boolean bolehDibatalkan()   { return this == MENUNGGU_BAYAR || this == MENUNGGU_KONFIRMASI || this == LUNAS; }
    /** Status yang masih mengunci slot jadwal. */
    public boolean mengunciSlot()      { return this == MENUNGGU_BAYAR || this == MENUNGGU_KONFIRMASI || this == LUNAS; }

    @Override public String toString() { return label; }
}

package model;

public enum JenisLapangan {
    FUTSAL("Futsal"), MINISOCCER("Minisoccer"), BASKET("Basket"),
    BADMINTON("Badminton"), PADEL("Padel");

    private final String label;
    JenisLapangan(String label) { this.label = label; }
    @Override public String toString() { return label; }
}

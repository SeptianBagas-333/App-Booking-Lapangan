package model;

public enum Member {
    BIASA(0), SILVER(5), GOLD(10);

    private final int persen;
    Member(int persen) { this.persen = persen; }
    public int getPersen() { return persen; }
}

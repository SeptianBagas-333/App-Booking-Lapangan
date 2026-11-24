package model;

public class DiskonTanpa implements Diskon {
    @Override public double hitungDiskon(double subtotal) { return 0; }
    @Override public String getNama() { return "Tanpa diskon"; }
}

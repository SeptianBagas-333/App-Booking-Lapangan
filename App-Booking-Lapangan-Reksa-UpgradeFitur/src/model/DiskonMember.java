package model;

public class DiskonMember implements Diskon {
    private final Member member;
    public DiskonMember(Member member) { this.member = member; }

    @Override public double hitungDiskon(double subtotal) { return subtotal * member.getPersen() / 100.0; }
    @Override public String getNama() { return "Member " + member + " " + member.getPersen() + "%"; }
}

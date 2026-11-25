package model;

public class Pelanggan extends User {
    private Member member;

    public Pelanggan(String id, String username, String passwordHash, String nama, Member member) {
        super(id, username, passwordHash, nama);
        this.member = member;
    }

    @Override public String getRole() { return "PELANGGAN"; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    /** Strategy diskon sesuai level member. */
    public Diskon getDiskon() {
        return member == Member.BIASA ? new DiskonTanpa() : new DiskonMember(member);
    }
}

import model.*;
import repository.*;
import service.*;
import view.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.time.*;

public class GuiSmoke {
    static void shot(JFrame f, String nama) throws Exception {
        f.setVisible(true);
        f.validate();
        BufferedImage img = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        f.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File("/tmp/" + nama + ".png"));
    }

    public static void main(String[] a) throws Exception {
        Path dir = Files.createTempDirectory("guismoke");
        UserRepository ur = new UserRepository(dir);
        LapanganRepository lr = new LapanganRepository(dir);
        BookingRepository br = new BookingRepository(dir, ur, lr);
        UpgradeMemberRepository upr = new UpgradeMemberRepository(dir, ur);
        BookingService bs = new BookingService(br, lr, dir.resolve("struk"));
        UpgradeMemberService us = new UpgradeMemberService(upr, ur);
        AuthService auth = new AuthService(ur);
        Pelanggan budi = (Pelanggan) auth.login("budi", "budi123");
        Pelanggan siti = (Pelanggan) auth.login("siti", "siti123");
        LocalDateTime now = LocalDateTime.now();
        LocalDate besok = now.toLocalDate().plusDays(2);
        Booking b1 = bs.buatBooking(budi, lr.findById("FUT1").get(), besok.atTime(18, 0), 2, now);
        bs.bayar(b1, Pembayaran.dari("E-Wallet"), now);
        Booking b2 = bs.buatBooking(siti, lr.findById("FUT2").get(), besok.atTime(10, 0), 1, now);
        bs.bayar(b2, Pembayaran.dari("Transfer"), now);
        bs.buatBooking(budi, lr.findById("FUT1").get(), besok.atTime(9, 0), 1, now);
        us.ajukanUpgrade(siti, Member.SILVER, Pembayaran.dari("Transfer"), now);

        SwingUtilities.invokeAndWait(() -> {
            try {
                shot(new LoginFrame(auth, bs, us), "1_login");
                PelangganFrame pf = new PelangganFrame(budi, bs, us, auth);
                // geser tanggal ke hari ber-booking: klik tombol ">" dua kali
                JTabbedPane tabs = (JTabbedPane) find(pf.getContentPane(), JTabbedPane.class);
                JPanel jadwal = (JPanel) tabs.getComponentAt(0);
                JButton next = (JButton) findText(jadwal, ">");
                next.doClick(); next.doClick();
                shot(pf, "2_jadwal");
                tabs.setSelectedIndex(1);
                shot(pf, "3_riwayat");
                shot(new AdminFrame((Admin) auth.login("admin", "admin123"), bs, us, auth), "4_admin");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("GUI SMOKE OK");
        System.exit(0);
    }

    static Component find(Container c, Class<?> k) {
        for (Component x : c.getComponents()) {
            if (k.isInstance(x)) return x;
            if (x instanceof Container) { Component r = find((Container) x, k); if (r != null) return r; }
        }
        return null;
    }
    static Component findText(Container c, String t) {
        for (Component x : c.getComponents()) {
            if (x instanceof JButton && t.equals(((JButton) x).getText())) return x;
            if (x instanceof Container) { Component r = findText((Container) x, t); if (r != null) return r; }
        }
        return null;
    }
}

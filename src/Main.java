import repository.*;
import service.*;
import view.LoginFrame;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        Path dir = Paths.get("data");
        UserRepository users = new UserRepository(dir);
        LapanganRepository lapangan = new LapanganRepository(dir);
        BookingRepository bookings = new BookingRepository(dir, users, lapangan);
        BookingService bs = new BookingService(bookings, lapangan, Paths.get("struk"));
        AuthService auth = new AuthService(users);
        bs.perbaruiStatus(LocalDateTime.now());   // lazy expiration saat aplikasi dibuka

        SwingUtilities.invokeLater(() -> new LoginFrame(auth, bs).setVisible(true));
    }
}

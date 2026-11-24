package view;

import exception.LoginGagalException;
import model.*;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthService auth;
    private final BookingService bs;
    private final JTextField tfUser = new JTextField(16);
    private final JPasswordField pfPass = new JPasswordField(16);

    public LoginFrame(AuthService auth, BookingService bs) {
        super("Sistem Booking Lapangan Olahraga");
        this.auth = auth;
        this.bs = bs;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel judul = new JLabel("Booking Lapangan Olahraga", SwingConstants.CENTER);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 20f));
        JLabel sub = new JLabel("Futsal | Minisoccer | Basket | Badminton | Padel", SwingConstants.CENTER);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        header.add(judul);
        header.add(sub);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Username"), g);
        g.gridx = 1; form.add(tfUser, g);
        g.gridx = 0; g.gridy = 1; form.add(new JLabel("Password"), g);
        g.gridx = 1; form.add(pfPass, g);

        JButton btnLogin = new JButton("Login");
        JButton btnDaftar = new JButton("Daftar Pelanggan");
        btnLogin.addActionListener(e -> login());
        btnDaftar.addActionListener(e -> daftar());
        getRootPane().setDefaultButton(btnLogin);

        JPanel bawah = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bawah.add(btnLogin);
        bawah.add(btnDaftar);
        JLabel hint = new JLabel("Demo: admin/admin123, budi/budi123, siti/siti123", SwingConstants.CENTER);
        hint.setForeground(Color.GRAY);
        JPanel south = new JPanel(new BorderLayout());
        south.add(bawah, BorderLayout.CENTER);
        south.add(hint, BorderLayout.SOUTH);
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        try {
            User u = auth.login(tfUser.getText(), new String(pfPass.getPassword()));
            bs.perbaruiStatus(java.time.LocalDateTime.now());
            JFrame next = (u instanceof Admin)
                    ? new AdminFrame((Admin) u, bs, auth)
                    : new PelangganFrame((Pelanggan) u, bs, auth);
            next.setVisible(true);
            dispose();
        } catch (LoginGagalException ex) {
            UiUtil.error(this, ex.getMessage());
            pfPass.setText("");
        }
    }

    private void daftar() {
        JTextField nama = new JTextField(), user = new JTextField();
        JPasswordField pass = new JPasswordField();
        Object[] isi = {"Nama lengkap", nama, "Username", user, "Password", pass};
        if (JOptionPane.showConfirmDialog(this, isi, "Registrasi Pelanggan", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            auth.registrasi(user.getText(), new String(pass.getPassword()), nama.getText());
            UiUtil.info(this, "Registrasi berhasil. Silakan login.");
            tfUser.setText(user.getText().trim());
            pfPass.setText("");
        } catch (LoginGagalException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }
}

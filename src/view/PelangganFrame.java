package view;

import exception.*;
import model.*;
import service.AuthService;
import service.BookingService;
import util.Fmt;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class PelangganFrame extends JFrame {
    private final Pelanggan user;
    private final BookingService bs;
    private final AuthService auth;

    private final JSpinner spTanggal = new JSpinner(new SpinnerDateModel());
    private final JComboBox<JenisLapangan> cbJenis = new JComboBox<>(JenisLapangan.values());
    private final DefaultTableModel mJadwal = UiUtil.modelTabel();
    private final JTable tJadwal = UiUtil.tabel(mJadwal);
    private List<Lapangan> kolomLapangan;

    private final DefaultTableModel mRiwayat = UiUtil.modelTabel();
    private final JTable tRiwayat = UiUtil.tabel(mRiwayat);

    public PelangganFrame(Pelanggan user, BookingService bs, AuthService auth) {
        super("Pelanggan - " + user.getNama());
        this.user = user;
        this.bs = bs;
        this.auth = auth;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cek Jadwal & Booking", buatTabJadwal());
        tabs.addTab("Riwayat Pemesanan", buatTabRiwayat());
        add(tabs, BorderLayout.CENTER);
        add(buatHeader(), BorderLayout.NORTH);

        refreshSemua();
        Timer timer = new Timer(30_000, e -> refreshSemua());   // memicu lazy expiration + refresh tampilan
        timer.start();
    }

    private JPanel buatHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel l = new JLabel("Halo, " + user.getNama() + "  |  Member: " + user.getMember()
                + " (" + user.getDiskon().getNama() + ")");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame(auth, bs).setVisible(true); });
        p.add(l, BorderLayout.WEST);
        p.add(logout, BorderLayout.EAST);
        return p;
    }

    // ================= TAB JADWAL =================
    private JPanel buatTabJadwal() {
        JPanel root = new JPanel(new BorderLayout());

        spTanggal.setEditor(new JSpinner.DateEditor(spTanggal, "dd-MM-yyyy"));
        spTanggal.setValue(new Date());
        JButton prev = new JButton("<"), next = new JButton(">");
        prev.addActionListener(e -> geserTanggal(-1));
        next.addActionListener(e -> geserTanggal(1));
        spTanggal.addChangeListener(e -> refreshJadwal());
        cbJenis.addActionListener(e -> refreshJadwal());

        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        atas.add(new JLabel("Tanggal:")); atas.add(prev); atas.add(spTanggal); atas.add(next);
        atas.add(Box.createHorizontalStrut(16));
        atas.add(new JLabel("Olahraga:")); atas.add(cbJenis);

        tJadwal.setCellSelectionEnabled(true);
        tJadwal.setDefaultRenderer(Object.class, new JadwalRenderer());
        tJadwal.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { klikSel(); }
        });

        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        legenda.add(kotak(UiUtil.HIJAU, "Kosong (klik untuk booking)"));
        legenda.add(kotak(UiUtil.MERAH, "Terisi"));
        legenda.add(kotak(UiUtil.BIRU, "Booking Anda"));
        legenda.add(kotak(UiUtil.ABU, "Tidak tersedia"));
        legenda.add(new JLabel("   Tarif jam sibuk 17.00-22.00 x1,25 | Sabtu-Minggu x1,15"));

        root.add(atas, BorderLayout.NORTH);
        root.add(new JScrollPane(tJadwal), BorderLayout.CENTER);
        root.add(legenda, BorderLayout.SOUTH);
        return root;
    }

    private JLabel kotak(Color c, String teks) {
        JLabel l = new JLabel("  " + teks + "  ");
        l.setOpaque(true);
        l.setBackground(c);
        return l;
    }

    private LocalDate tanggalDipilih() {
        return ((Date) spTanggal.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void geserTanggal(int hari) {
        spTanggal.setValue(Date.from(tanggalDipilih().plusDays(hari).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void refreshJadwal() {
        LocalDateTime now = LocalDateTime.now();
        bs.perbaruiStatus(now);
        LocalDate tgl = tanggalDipilih();
        kolomLapangan = bs.getLapangan((JenisLapangan) cbJenis.getSelectedItem());

        String[] kolom = new String[kolomLapangan.size() + 1];
        kolom[0] = "Jam";
        for (int i = 0; i < kolomLapangan.size(); i++) kolom[i + 1] = kolomLapangan.get(i).getNama();

        int jumlahJam = BookingService.JAM_TUTUP - BookingService.JAM_BUKA;
        Object[][] data = new Object[jumlahJam][kolom.length];
        for (int i = 0; i < jumlahJam; i++) {
            int jam = BookingService.JAM_BUKA + i;
            data[i][0] = String.format("%02d:00 - %02d:00", jam, jam + 1);
            for (int j = 0; j < kolomLapangan.size(); j++) {
                LocalDateTime t = tgl.atTime(jam, 0);
                Booking b = bs.cariBookingAktif(kolomLapangan.get(j), t);
                if (b != null) data[i][j + 1] = b.getPelanggan().getId().equals(user.getId()) ? "Anda" : "Terisi";
                else if (t.isBefore(now.plusMinutes(BookingService.MINIMAL_SEBELUM_MAIN_MENIT))) data[i][j + 1] = "-";
                else data[i][j + 1] = "Kosong";
            }
        }
        mJadwal.setDataVector(data, kolom);
        tJadwal.getColumnModel().getColumn(0).setPreferredWidth(120);
    }

    private void klikSel() {
        int r = tJadwal.getSelectedRow(), c = tJadwal.getSelectedColumn();
        if (r < 0 || c < 1) return;
        if (!"Kosong".equals(mJadwal.getValueAt(r, c))) return;
        Lapangan l = kolomLapangan.get(c - 1);
        LocalDateTime mulai = tanggalDipilih().atTime(BookingService.JAM_BUKA + r, 0);
        dialogBooking(l, mulai);
    }

    private void dialogBooking(Lapangan l, LocalDateTime mulai) {
        // durasi maksimum: sampai slot berikutnya terisi / tutup / batas 3 jam
        int maks = 0;
        for (int d = 1; d <= BookingService.MAKS_DURASI; d++) {
            if (mulai.getHour() + d > BookingService.JAM_TUTUP) break;
            if (!bs.isSlotTersedia(l, mulai, d)) break;
            maks = d;
        }
        Integer[] pilihan = new Integer[maks];
        for (int i = 0; i < maks; i++) pilihan[i] = i + 1;
        JComboBox<Integer> cbDurasi = new JComboBox<>(pilihan);
        JTextArea rincian = new JTextArea(7, 30);
        rincian.setEditable(false);
        rincian.setBackground(new Color(245, 245, 245));
        Runnable update = () -> {
            int d = (Integer) cbDurasi.getSelectedItem();
            BookingService.Rincian r = bs.hitungRincian(l, mulai, d, user);
            rincian.setText(String.format(
                    "Lapangan : %s (%s)%nTanggal  : %s%nJam      : %s - %s%n%nBiaya sewa : %s%nDiskon (%s) : -%s%nTOTAL      : %s",
                    l.getNama(), l.getJenis(), mulai.format(Fmt.TANGGAL), mulai.format(Fmt.JAM),
                    mulai.plusHours(d).format(Fmt.JAM), Fmt.rp(r.sewa), r.namaDiskon, Fmt.rp(r.diskon), Fmt.rp(r.total)));
        };
        cbDurasi.addActionListener(e -> update.run());
        update.run();

        JPanel p = new JPanel(new BorderLayout(6, 6));
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        atas.add(new JLabel("Durasi (jam):")); atas.add(cbDurasi);
        if (!l.getKeterangan().isEmpty()) atas.add(new JLabel("  (" + l.getKeterangan() + ")"));
        p.add(atas, BorderLayout.NORTH);
        p.add(rincian, BorderLayout.CENTER);

        if (JOptionPane.showConfirmDialog(this, p, "Booking Lapangan", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;
        try {
            Booking b = bs.buatBooking(user, l, mulai, (Integer) cbDurasi.getSelectedItem(), LocalDateTime.now());
            refreshSemua();
            if (UiUtil.konfirmasi(this, "Booking " + b.getId() + " dibuat (Menunggu Bayar).\nBayar sekarang? "
                    + "Batas bayar " + b.getBatasBayar().format(Fmt.JAM) + "."))
                dialogBayar(b);
        } catch (JadwalBentrokException | IllegalArgumentException ex) {
            UiUtil.error(this, ex.getMessage());
            refreshSemua();
        }
    }

    // ================= TAB RIWAYAT =================
    private JPanel buatTabRiwayat() {
        JPanel root = new JPanel(new BorderLayout());
        JButton bayar = new JButton("Bayar"), batal = new JButton("Batalkan"),
                struk = new JButton("Cetak Struk"), refresh = new JButton("Refresh");
        bayar.addActionListener(e -> aksiBayar());
        batal.addActionListener(e -> aksiBatal());
        struk.addActionListener(e -> aksiStruk());
        refresh.addActionListener(e -> refreshSemua());
        root.add(UiUtil.panelTombol(bayar, batal, struk, refresh), BorderLayout.NORTH);
        root.add(new JScrollPane(tRiwayat), BorderLayout.CENTER);
        return root;
    }

    private Booking bookingTerpilih() {
        String id = UiUtil.idTerpilih(tRiwayat);
        if (id == null) { UiUtil.error(this, "Pilih salah satu booking pada tabel terlebih dahulu."); return null; }
        return bs.cariById(id).orElse(null);
    }

    private void aksiBayar() {
        Booking b = bookingTerpilih();
        if (b == null) return;
        bs.perbaruiStatus(LocalDateTime.now());
        if (!b.getStatus().bolehDibayar()) {
            refreshSemua();
            UiUtil.error(this, "Booking berstatus '" + b.getStatus() + "' tidak dapat dibayar.");
            return;
        }
        dialogBayar(b);
    }

    private void dialogBayar(Booking b) {
        JComboBox<String> cb = new JComboBox<>(Pembayaran.DAFTAR_METODE);
        Object[] isi = {String.format("Booking %s - %s%n%s %s-%s%nDiskon : -%s%nTOTAL  : %s",
                b.getId(), b.getLapangan().getNama(), b.getMulai().format(Fmt.TANGGAL),
                b.getMulai().format(Fmt.JAM), b.getSelesai().format(Fmt.JAM),
                Fmt.rp(b.getDiskon()), Fmt.rp(b.getTotal())),
                "Metode pembayaran:", cb,
                "E-Wallet langsung lunas. Transfer/Tunai menunggu konfirmasi admin."};
        if (JOptionPane.showConfirmDialog(this, isi, "Pembayaran", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            bs.bayar(b, Pembayaran.dari((String) cb.getSelectedItem()), LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, b.getStatus() == StatusBooking.LUNAS
                    ? "Pembayaran berhasil. Booking LUNAS."
                    : "Pembayaran dicatat. Menunggu konfirmasi admin.");
        } catch (BatasBayarLewatException | IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void aksiBatal() {
        Booking b = bookingTerpilih();
        if (b == null) return;
        LocalDateTime now = LocalDateTime.now();
        bs.perbaruiStatus(now);
        if (!b.bolehDibatalkan(now)) {
            refreshSemua();
            UiUtil.error(this, "Booking ini tidak dapat dibatalkan (status: " + b.getStatus() + ").");
            return;
        }
        String pesan = "Batalkan booking " + b.getId() + "?";
        if (b.getStatus() == StatusBooking.LUNAS)
            pesan += "\nRefund yang Anda terima: " + Fmt.rp(b.hitungRefund(now))
                    + "\n(>=24 jam: 100%, 3-24 jam: 50%, <3 jam: 0%)";
        if (!UiUtil.konfirmasi(this, pesan)) return;
        try {
            double refund = bs.batalkan(b, LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Booking dibatalkan." + (refund > 0 ? "\nRefund: " + Fmt.rp(refund) : ""));
        } catch (PembatalanTidakValidException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void aksiStruk() {
        Booking b = bookingTerpilih();
        if (b == null) return;
        try {
            Path f = bs.cetakStruk(b);
            JTextArea ta = new JTextArea(bs.buatTeksStruk(b));
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            ta.setEditable(false);
            JOptionPane.showMessageDialog(this, ta, "Struk disimpan: " + f, JOptionPane.PLAIN_MESSAGE);
        } catch (IllegalStateException | IOException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void refreshRiwayat() {
        List<Booking> data = new java.util.ArrayList<>(bs.getBookingPelanggan(user));
        java.util.Collections.reverse(data);
        UiUtil.isiTabelBooking(tRiwayat, mRiwayat, data, false, LocalDateTime.now());
    }

    private void refreshSemua() {
        bs.perbaruiStatus(LocalDateTime.now());
        refreshJadwal();
        refreshRiwayat();
    }

    /** Warna sel jadwal sesuai isinya. */
    private static class JadwalRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(CENTER);
            setForeground(Color.BLACK);
            String s = String.valueOf(v);
            if (c == 0) { setBackground(new Color(240, 240, 240)); setFont(getFont().deriveFont(Font.BOLD)); return this; }
            setFont(getFont().deriveFont(Font.PLAIN));
            switch (s) {
                case "Kosong": setBackground(UiUtil.HIJAU); break;
                case "Terisi": setBackground(UiUtil.MERAH); break;
                case "Anda": setBackground(UiUtil.BIRU); break;
                default: setBackground(UiUtil.ABU);
            }
            if (sel && "Kosong".equals(s)) setBackground(UiUtil.HIJAU.darker());
            return this;
        }
    }
}

package view;

import exception.PembatalanTidakValidException;
import model.*;
import service.AuthService;
import service.BookingService;
import util.Fmt;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminFrame extends JFrame {
    private static final String SEMUA = "Semua status";

    private final Admin admin;
    private final BookingService bs;
    private final AuthService auth;

    private final DefaultTableModel mBooking = UiUtil.modelTabel();
    private final JTable tBooking = UiUtil.tabel(mBooking);
    private final JComboBox<Object> cbFilter = new JComboBox<>();
    private final DefaultTableModel mLapangan = UiUtil.modelTabel();
    private final JTextArea taLaporan = new JTextArea();

    public AdminFrame(Admin admin, BookingService bs, AuthService auth) {
        super("Admin - " + admin.getNama());
        this.admin = admin;
        this.bs = bs;
        this.auth = auth;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Semua Booking", buatTabBooking());
        tabs.addTab("Daftar Lapangan", buatTabLapangan());
        tabs.addTab("Laporan Pendapatan", buatTabLaporan());
        tabs.addChangeListener(e -> refreshSemua());

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel l = new JLabel("Panel Admin - " + admin.getNama());
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginFrame(auth, bs).setVisible(true); });
        header.add(l, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        refreshSemua();
        new Timer(30_000, e -> refreshSemua()).start();
    }

    private JPanel buatTabBooking() {
        JPanel root = new JPanel(new BorderLayout());
        cbFilter.addItem(SEMUA);
        for (StatusBooking s : StatusBooking.values()) cbFilter.addItem(s);
        cbFilter.addActionListener(e -> refreshBooking());

        JButton konfirmasi = new JButton("Konfirmasi Pembayaran");
        JButton batal = new JButton("Batalkan");
        JButton refresh = new JButton("Refresh");
        konfirmasi.addActionListener(e -> aksiKonfirmasi());
        batal.addActionListener(e -> aksiBatal());
        refresh.addActionListener(e -> refreshSemua());

        JPanel atas = UiUtil.panelTombol(konfirmasi, batal, refresh);
        atas.add(new JLabel("   Filter:"));
        atas.add(cbFilter);
        root.add(atas, BorderLayout.NORTH);
        root.add(new JScrollPane(tBooking), BorderLayout.CENTER);
        return root;
    }

    private JPanel buatTabLapangan() {
        JPanel root = new JPanel(new BorderLayout());
        JTable t = UiUtil.tabel(mLapangan);
        root.add(new JScrollPane(t), BorderLayout.CENTER);
        JLabel cat = new JLabel("  Data lapangan disimpan di data/lapangan.csv (edit manual lalu jalankan ulang aplikasi).");
        cat.setForeground(Color.GRAY);
        root.add(cat, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buatTabLaporan() {
        JPanel root = new JPanel(new BorderLayout());
        taLaporan.setEditable(false);
        taLaporan.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        taLaporan.setMargin(new Insets(12, 12, 12, 12));
        root.add(new JScrollPane(taLaporan), BorderLayout.CENTER);
        return root;
    }

    private Booking terpilih() {
        String id = UiUtil.idTerpilih(tBooking);
        if (id == null) { UiUtil.error(this, "Pilih salah satu booking pada tabel terlebih dahulu."); return null; }
        return bs.cariById(id).orElse(null);
    }

    private void aksiKonfirmasi() {
        Booking b = terpilih();
        if (b == null) return;
        try {
            bs.konfirmasi(b, LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Pembayaran " + b.getId() + " (" + b.getMetode() + ") dikonfirmasi. Status: LUNAS.");
        } catch (IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void aksiBatal() {
        Booking b = terpilih();
        if (b == null) return;
        LocalDateTime now = LocalDateTime.now();
        bs.perbaruiStatus(now);
        if (!b.bolehDibatalkan(now)) {
            refreshSemua();
            UiUtil.error(this, "Booking ini tidak dapat dibatalkan (status: " + b.getStatus() + ").");
            return;
        }
        String pesan = "Batalkan booking " + b.getId() + " milik " + b.getPelanggan().getNama() + "?";
        if (b.getStatus() == StatusBooking.LUNAS)
            pesan += "\nRefund sesuai kebijakan: " + Fmt.rp(b.hitungRefund(now));
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

    private void refreshBooking() {
        List<Booking> data = new ArrayList<>();
        Object f = cbFilter.getSelectedItem();
        for (Booking b : bs.getSemuaBooking())
            if (f == null || SEMUA.equals(f) || b.getStatus() == f) data.add(b);
        java.util.Collections.reverse(data);
        UiUtil.isiTabelBooking(tBooking, mBooking, data, true, LocalDateTime.now());
    }

    private void refreshLapangan() {
        Object[][] baris = new Object[bs.getLapangan().size()][];
        int i = 0;
        for (Lapangan l : bs.getLapangan())
            baris[i++] = new Object[]{l.getId(), l.getNama(), l.getJenis(), Fmt.rp(l.getTarifDasar()) + "/jam",
                    Fmt.rp(l.getBiayaTambahan()), l.getKeterangan()};
        mLapangan.setDataVector(baris, new String[]{"ID", "Nama", "Jenis", "Tarif Dasar", "Biaya Tambahan", "Keterangan"});
    }

    private void refreshLaporan() {
        StringBuilder sb = new StringBuilder("LAPORAN PENDAPATAN\n==================\n\n");
        sb.append("Jumlah booking per status\n");
        for (Map.Entry<StatusBooking, Long> e : bs.jumlahPerStatus().entrySet())
            sb.append(String.format("  %-22s : %d%n", e.getKey(), e.getValue()));
        sb.append("\nPendapatan bersih per jenis lapangan (setelah refund)\n");
        for (Map.Entry<JenisLapangan, Double> e : bs.pendapatanPerJenis().entrySet())
            sb.append(String.format("  %-22s : %s%n", e.getKey(), Fmt.rp(e.getValue())));
        sb.append(String.format("%nTOTAL PENDAPATAN BERSIH     : %s%n", Fmt.rp(bs.totalPendapatan())));
        taLaporan.setText(sb.toString());
    }

    private void refreshSemua() {
        bs.perbaruiStatus(LocalDateTime.now());
        refreshBooking();
        refreshLapangan();
        refreshLaporan();
    }
}

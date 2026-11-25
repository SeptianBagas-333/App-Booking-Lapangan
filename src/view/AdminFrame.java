package view;

import exception.PembatalanTidakValidException;
import model.*;
import service.AuthService;
import service.BookingService;
import service.UpgradeMemberService;
import util.Fmt;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Layar utama admin dengan sidebar navigasi modern, manajemen transaksi, dan laporan KPI visual. */
public class AdminFrame extends JFrame {
    private static final String SEMUA = "Semua status";

    private static final String PAGE_BOOKING = "BOOKING";
    private static final String PAGE_UPGRADE = "UPGRADE";
    private static final String PAGE_LAPANGAN = "LAPANGAN";
    private static final String PAGE_LAPORAN = "LAPORAN";

    private final Admin admin;
    private final BookingService bs;
    private final UpgradeMemberService us;
    private final AuthService auth;
    private final Timer timer;

    private final CardLayout pageCards = new CardLayout();
    private final JPanel pageHost = new JPanel(pageCards);
    private final List<UiUtil.SidebarNavButton> navButtons = new ArrayList<>();

    // Halaman Manajemen Booking
    private final DefaultTableModel mBooking = UiUtil.modelTabel();
    private final JTable tBooking = UiUtil.tabel(mBooking);
    private final JComboBox<Object> cbFilter = new JComboBox<>();

    // Halaman Upgrade Member
    private final DefaultTableModel mUpgrade = UiUtil.modelTabel();
    private final JTable tUpgrade = UiUtil.tabel(mUpgrade);
    private final JComboBox<Object> cbFilterUpgrade = new JComboBox<>();

    // Halaman Daftar Lapangan
    private final DefaultTableModel mLapangan = UiUtil.modelTabel();
    private final JTable tLapangan = UiUtil.tabel(mLapangan);

    // Halaman Laporan Pendapatan (KPI + breakdown visual)
    private final UiUtil.KpiCard kpiTotal = new UiUtil.KpiCard(
            "Total Pendapatan", "Rp0", "Gabungan", UiUtil.PRIMARY_SOFT_BG, UiUtil.PRIMARY_END);
    private final UiUtil.KpiCard kpiBooking = new UiUtil.KpiCard(
            "Booking Bersih", "Rp0", "Booking", UiUtil.BADGE_GREEN_BG, UiUtil.BADGE_GREEN_TEXT);
    private final UiUtil.KpiCard kpiUpgrade = new UiUtil.KpiCard(
            "Upgrade Member", "Rp0", "Lunas", UiUtil.BADGE_BLUE_BG, UiUtil.BADGE_BLUE_TEXT);
    private final UiUtil.KpiCard kpiTransaksi = new UiUtil.KpiCard(
            "Transaksi Sukses", "0", "Aktif", UiUtil.BADGE_AMBER_BG, UiUtil.BADGE_AMBER_TEXT);

    private final JPanel panelPerJenis = new JPanel();
    private final JPanel panelPerStatus = new JPanel();
    private final JLabel lblSubtotalJenis = new JLabel();

    public AdminFrame(Admin admin, BookingService bs, UpgradeMemberService us, AuthService auth) {
        super("ArenaBook - Admin (" + admin.getNama() + ")");
        this.admin = admin;
        this.bs = bs;
        this.us = us;
        this.auth = auth;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1220, 760);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        UiUtil.PillLabel adminBadge = new UiUtil.PillLabel(
                "ADMIN", UiUtil.PRIMARY_SOFT_BG, UiUtil.PRIMARY_START, UiUtil.PRIMARY_END, true);
        UiUtil.AppSidebar sidebar = new UiUtil.AppSidebar(
                "Panel Administrasi",
                admin.getNama(),
                "Pengelola Lapangan",
                adminBadge,
                this::logout
        );

        UiUtil.SidebarNavButton navBooking = new UiUtil.SidebarNavButton("Manajemen Booking", UiUtil.NavIcon.LIST);
        UiUtil.SidebarNavButton navUpgrade = new UiUtil.SidebarNavButton("Upgrade Member", UiUtil.NavIcon.CROWN);
        UiUtil.SidebarNavButton navLapangan = new UiUtil.SidebarNavButton("Daftar Lapangan", UiUtil.NavIcon.COURT);
        UiUtil.SidebarNavButton navLaporan = new UiUtil.SidebarNavButton("Laporan Pendapatan", UiUtil.NavIcon.CHART);
        navButtons.add(navBooking);
        navButtons.add(navUpgrade);
        navButtons.add(navLapangan);
        navButtons.add(navLaporan);

        String[] keys = {PAGE_BOOKING, PAGE_UPGRADE, PAGE_LAPANGAN, PAGE_LAPORAN};
        for (int i = 0; i < navButtons.size(); i++) {
            final int idx = i;
            UiUtil.SidebarNavButton btn = navButtons.get(i);
            btn.addActionListener(e -> showPage(keys[idx], idx));
            sidebar.addNavButton(btn);
        }

        pageHost.setOpaque(false);
        pageHost.add(buatHalamanBooking(), PAGE_BOOKING);
        pageHost.add(buatHalamanUpgrade(), PAGE_UPGRADE);
        pageHost.add(buatHalamanLapangan(), PAGE_LAPANGAN);
        pageHost.add(buatHalamanLaporan(), PAGE_LAPORAN);

        UiUtil.GradientCanvasPanel contentHost =
                new UiUtil.GradientCanvasPanel(UiUtil.BG_CANVAS_START, UiUtil.BG_CANVAS_END, true);
        contentHost.setLayout(new BorderLayout());
        contentHost.setBorder(new EmptyBorder(22, 26, 22, 26));
        contentHost.add(pageHost, BorderLayout.CENTER);

        JPanel shell = new JPanel(new BorderLayout());
        shell.add(sidebar, BorderLayout.WEST);
        shell.add(contentHost, BorderLayout.CENTER);
        setContentPane(shell);

        showPage(PAGE_BOOKING, 0);

        timer = new Timer(30_000, e -> refreshSemua());
        timer.start();
    }

    private void showPage(String key, int activeIdx) {
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setActive(i == activeIdx);
        }
        pageCards.show(pageHost, key);
        refreshSemua();
    }

    private void logout() {
        timer.stop();
        dispose();
        UiUtil.tampilkanMaksimal(new LoginFrame(auth, bs, us));
    }

    // ================= HALAMAN 1: MANAJEMEN BOOKING =================
    private JPanel buatHalamanBooking() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        JPanel header = UiUtil.createPageHeader(
                "Manajemen Booking",
                "Konfirmasi pembayaran transfer/tunai dan kelola seluruh reservasi pelanggan.",
                UiUtil.createAutoRefreshBadge()
        );

        cbFilter.addItem(SEMUA);
        for (StatusBooking s : StatusBooking.values()) cbFilter.addItem(s);
        UiUtil.styleComboBox(cbFilter);
        cbFilter.setPreferredSize(new Dimension(190, 38));
        cbFilter.addActionListener(e -> refreshBooking());

        UiUtil.ModernButton btnKonfirmasi = new UiUtil.ModernButton("Konfirmasi Pembayaran", UiUtil.ModernButton.Variant.PRIMARY);
        UiUtil.ModernButton btnBatal = new UiUtil.ModernButton("Batalkan Booking", UiUtil.ModernButton.Variant.DANGER_SOFT);
        UiUtil.ModernButton btnRefresh = new UiUtil.ModernButton("Refresh", UiUtil.ModernButton.Variant.SECONDARY);
        btnKonfirmasi.addActionListener(e -> aksiKonfirmasi());
        btnBatal.addActionListener(e -> aksiBatal());
        btnRefresh.addActionListener(e -> refreshSemua());

        UiUtil.ShadowCardPanel toolbar = new UiUtil.ShadowCardPanel(14, 6);
        toolbar.setLayout(new BorderLayout(12, 0));
        toolbar.setContentPadding(10, 14, 10, 14);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(btnKonfirmasi);
        left.add(btnBatal);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter Status:");
        lblFilter.setFont(UiUtil.fontLabel(13f));
        lblFilter.setForeground(UiUtil.TEXT_BODY);
        right.add(lblFilter);
        right.add(cbFilter);
        right.add(btnRefresh);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        JScrollPane sp = new JScrollPane(tBooking);
        UiUtil.styleScrollPane(sp);

        UiUtil.ShadowCardPanel tableCard = new UiUtil.ShadowCardPanel(16, 7);
        tableCard.setLayout(new BorderLayout());
        tableCard.setContentPadding(14, 14, 14, 14);
        tableCard.add(sp, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }

    // ================= HALAMAN 2: UPGRADE MEMBER =================
    private JPanel buatHalamanUpgrade() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        JPanel header = UiUtil.createPageHeader(
                "Upgrade Member",
                "Konfirmasi pengajuan menunggu verifikasi atau batalkan & downgrade transaksi lunas.",
                UiUtil.createAutoRefreshBadge()
        );

        cbFilterUpgrade.addItem(SEMUA);
        for (StatusUpgrade s : StatusUpgrade.values()) cbFilterUpgrade.addItem(s);
        UiUtil.styleComboBox(cbFilterUpgrade);
        cbFilterUpgrade.setPreferredSize(new Dimension(190, 38));
        cbFilterUpgrade.addActionListener(e -> refreshUpgrade());

        UiUtil.ModernButton btnKonfirmasi = new UiUtil.ModernButton("Konfirmasi Pembayaran", UiUtil.ModernButton.Variant.PRIMARY);
        UiUtil.ModernButton btnBatal = new UiUtil.ModernButton("Batalkan Pengajuan", UiUtil.ModernButton.Variant.SECONDARY);
        UiUtil.ModernButton btnDowngrade = new UiUtil.ModernButton("Batalkan & Downgrade", UiUtil.ModernButton.Variant.DANGER);
        UiUtil.ModernButton btnRefresh = new UiUtil.ModernButton("Refresh", UiUtil.ModernButton.Variant.SECONDARY);

        btnKonfirmasi.setToolTipText("Setujui pengajuan berstatus Menunggu Konfirmasi");
        btnBatal.setToolTipText("Tolak pengajuan berstatus Menunggu Konfirmasi");
        btnDowngrade.setToolTipText("Batalkan transaksi LUNAS terbaru & turunkan tier member");

        btnKonfirmasi.addActionListener(e -> aksiKonfirmasiUpgrade());
        btnBatal.addActionListener(e -> aksiBatalUpgrade());
        btnDowngrade.addActionListener(e -> aksiBatalkanDanDowngrade());
        btnRefresh.addActionListener(e -> refreshSemua());

        UiUtil.ShadowCardPanel toolbar = new UiUtil.ShadowCardPanel(14, 6);
        toolbar.setLayout(new BorderLayout(10, 0));
        toolbar.setContentPadding(10, 14, 10, 14);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(btnKonfirmasi);
        left.add(btnBatal);
        left.add(btnDowngrade);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(UiUtil.fontLabel(13f));
        lblFilter.setForeground(UiUtil.TEXT_BODY);
        right.add(lblFilter);
        right.add(cbFilterUpgrade);
        right.add(btnRefresh);

        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(right, BorderLayout.EAST);

        JScrollPane sp = new JScrollPane(tUpgrade);
        UiUtil.styleScrollPane(sp);

        UiUtil.ShadowCardPanel tableCard = new UiUtil.ShadowCardPanel(16, 7);
        tableCard.setLayout(new BorderLayout());
        tableCard.setContentPadding(14, 14, 14, 14);
        tableCard.add(sp, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }

    // ================= HALAMAN 3: DAFTAR LAPANGAN =================
    private JPanel buatHalamanLapangan() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        JPanel header = UiUtil.createPageHeader(
                "Daftar Lapangan",
                "Daftar seluruh fasilitas lapangan olahraga beserta tarif dasar dan biaya tambahan.",
                null
        );

        JScrollPane sp = new JScrollPane(tLapangan);
        UiUtil.styleScrollPane(sp);

        JLabel cat = new JLabel("Data lapangan disimpan di data/lapangan.csv (edit manual lalu jalankan ulang aplikasi).");
        cat.setFont(UiUtil.fontBody(12f));
        cat.setForeground(UiUtil.TEXT_MUTED);
        cat.setBorder(new EmptyBorder(10, 4, 0, 0));

        UiUtil.ShadowCardPanel tableCard = new UiUtil.ShadowCardPanel(16, 7);
        tableCard.setLayout(new BorderLayout());
        tableCard.setContentPadding(14, 16, 14, 16);
        tableCard.add(sp, BorderLayout.CENTER);
        tableCard.add(cat, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }

    // ================= HALAMAN 4: LAPORAN PENDAPATAN =================
    private JPanel buatHalamanLaporan() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setOpaque(false);

        UiUtil.ModernButton btnRefresh = new UiUtil.ModernButton("Refresh Laporan", UiUtil.ModernButton.Variant.SECONDARY);
        btnRefresh.addActionListener(e -> refreshSemua());

        JPanel header = UiUtil.createPageHeader(
                "Laporan Pendapatan",
                "Ringkasan pendapatan bersih booking, upgrade member, serta distribusi status transaksi.",
                btnRefresh
        );

        // Baris 4 KpiCard sejajar
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.add(kpiTotal);
        kpiRow.add(kpiBooking);
        kpiRow.add(kpiUpgrade);
        kpiRow.add(kpiTransaksi);

        // Dua ShadowCardPanel berdampingan di bawah KPI
        UiUtil.ShadowCardPanel cardJenis = new UiUtil.ShadowCardPanel(16, 7);
        cardJenis.setLayout(new BorderLayout(0, 12));
        cardJenis.setContentPadding(18, 20, 16, 20);

        JLabel titleJenis = new JLabel("Pendapatan Bersih per Jenis Lapangan");
        titleJenis.setFont(UiUtil.fontHeading(15.5f));
        titleJenis.setForeground(UiUtil.TEXT_HEADING);
        JLabel subJenis = new JLabel("Setelah memperhitungkan pengembalian dana (refund)");
        subJenis.setFont(UiUtil.fontBody(12f));
        subJenis.setForeground(UiUtil.TEXT_MUTED);
        JPanel headJenis = new JPanel();
        headJenis.setOpaque(false);
        headJenis.setLayout(new BoxLayout(headJenis, BoxLayout.Y_AXIS));
        headJenis.add(titleJenis);
        headJenis.add(Box.createVerticalStrut(2));
        headJenis.add(subJenis);

        panelPerJenis.setOpaque(false);
        panelPerJenis.setLayout(new BoxLayout(panelPerJenis, BoxLayout.Y_AXIS));

        JScrollPane spJenis = new JScrollPane(panelPerJenis);
        UiUtil.styleScrollPane(spJenis);
        spJenis.setBorder(null);

        JPanel footJenis = new JPanel(new BorderLayout());
        footJenis.setOpaque(false);
        footJenis.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER_SUBTLE),
                new EmptyBorder(12, 0, 0, 0)
        ));
        JLabel lblSubCap = new JLabel("Subtotal Pendapatan Booking");
        lblSubCap.setFont(UiUtil.fontLabel(13.5f));
        lblSubCap.setForeground(UiUtil.TEXT_BODY);
        lblSubtotalJenis.setFont(UiUtil.fontHeading(15.5f));
        lblSubtotalJenis.setForeground(UiUtil.PRIMARY_END);
        footJenis.add(lblSubCap, BorderLayout.WEST);
        footJenis.add(lblSubtotalJenis, BorderLayout.EAST);

        cardJenis.add(headJenis, BorderLayout.NORTH);
        cardJenis.add(spJenis, BorderLayout.CENTER);
        cardJenis.add(footJenis, BorderLayout.SOUTH);

        // Kartu kanan: Ringkasan jumlah booking & upgrade per status
        UiUtil.ShadowCardPanel cardStatus = new UiUtil.ShadowCardPanel(16, 7);
        cardStatus.setLayout(new BorderLayout(0, 12));
        cardStatus.setContentPadding(18, 20, 16, 20);

        JLabel titleStatus = new JLabel("Ringkasan Transaksi per Status");
        titleStatus.setFont(UiUtil.fontHeading(15.5f));
        titleStatus.setForeground(UiUtil.TEXT_HEADING);
        JLabel subStatus = new JLabel("Distribusi status seluruh booking dan pengajuan upgrade member");
        subStatus.setFont(UiUtil.fontBody(12f));
        subStatus.setForeground(UiUtil.TEXT_MUTED);
        JPanel headStatus = new JPanel();
        headStatus.setOpaque(false);
        headStatus.setLayout(new BoxLayout(headStatus, BoxLayout.Y_AXIS));
        headStatus.add(titleStatus);
        headStatus.add(Box.createVerticalStrut(2));
        headStatus.add(subStatus);

        panelPerStatus.setOpaque(false);
        panelPerStatus.setLayout(new BoxLayout(panelPerStatus, BoxLayout.Y_AXIS));

        JScrollPane spStatus = new JScrollPane(panelPerStatus);
        UiUtil.styleScrollPane(spStatus);
        spStatus.setBorder(null);

        cardStatus.add(headStatus, BorderLayout.NORTH);
        cardStatus.add(spStatus, BorderLayout.CENTER);

        JPanel detailGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        detailGrid.setOpaque(false);
        detailGrid.add(cardJenis);
        detailGrid.add(cardStatus);

        JPanel topStack = new JPanel(new BorderLayout(0, 14));
        topStack.setOpaque(false);
        topStack.add(header, BorderLayout.NORTH);
        topStack.add(kpiRow, BorderLayout.SOUTH);

        root.add(topStack, BorderLayout.NORTH);
        root.add(detailGrid, BorderLayout.CENTER);
        return root;
    }

    // ================= AKSI BOOKING =================
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
        if (b.getStatus() == StatusBooking.LUNAS) {
            pesan += "\n\nRefund sesuai kebijakan: " + Fmt.rp(b.hitungRefund(now));
        }
        if (!UiUtil.konfirmasiDanger(this, "Batalkan Booking", pesan, "Ya, Batalkan")) return;
        try {
            double refund = bs.batalkan(b, LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Booking dibatalkan." + (refund > 0 ? "\nRefund: " + Fmt.rp(refund) : ""));
        } catch (PembatalanTidakValidException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    // ================= AKSI UPGRADE MEMBER =================
    private UpgradeMember upgradeTerpilih() {
        String id = UiUtil.idTerpilih(tUpgrade);
        if (id == null) { UiUtil.error(this, "Pilih salah satu pengajuan upgrade pada tabel terlebih dahulu."); return null; }
        return us.cariById(id).orElse(null);
    }

    private void aksiKonfirmasiUpgrade() {
        UpgradeMember u = upgradeTerpilih();
        if (u == null) return;
        if (!u.getStatus().bolehDikonfirmasi()) {
            UiUtil.error(this, "Hanya pengajuan 'Menunggu Konfirmasi' yang dapat dikonfirmasi.");
            return;
        }
        String pesan = String.format("Konfirmasi pembayaran upgrade %s?%n%nPelanggan : %s%nUpgrade   : %s -> %s%nBiaya     : %s (%s)",
                u.getId(), u.getPelanggan().getNama(), u.getDariTier(), u.getKeTier(),
                Fmt.rp(u.getBiaya()), u.getMetode());
        if (!UiUtil.konfirmasi(this, pesan)) return;
        try {
            us.konfirmasi(u, LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Upgrade " + u.getId() + " dikonfirmasi. Tier member "
                    + u.getPelanggan().getNama() + " kini " + u.getKeTier() + ".");
        } catch (IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void aksiBatalUpgrade() {
        UpgradeMember u = upgradeTerpilih();
        if (u == null) return;
        if (!u.getStatus().bolehDibatalkan()) {
            UiUtil.error(this, "Hanya pengajuan 'Menunggu Konfirmasi' yang dapat dibatalkan.");
            return;
        }
        String pesan = "Batalkan pengajuan upgrade " + u.getId() + " milik " + u.getPelanggan().getNama()
                + " (" + u.getDariTier() + " -> " + u.getKeTier() + ")?";
        if (!UiUtil.konfirmasiDanger(this, "Batalkan Pengajuan Upgrade", pesan, "Ya, Batalkan")) return;
        try {
            us.batalkan(u, LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Pengajuan upgrade " + u.getId() + " dibatalkan.");
        } catch (IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void aksiBatalkanDanDowngrade() {
        UpgradeMember u = upgradeTerpilih();
        if (u == null) return;
        try {
            us.validasiBatalkanDanDowngrade(u);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
            return;
        }
        String pesan = String.format(
                "Batalkan transaksi upgrade %s dan turunkan tier member?%n%n"
                + "Pelanggan      : %s%n"
                + "Penurunan tier : %s -> %s%n"
                + "Nominal        : %s%n%n"
                + "Transaksi ini akan diubah menjadi DIBATALKAN dan dihapus dari laporan pendapatan.",
                u.getId(), u.getPelanggan().getNama(), u.getKeTier(), u.getDariTier(), Fmt.rp(u.getBiaya()));
        if (!UiUtil.konfirmasiDanger(this, "Batalkan & Downgrade Member", pesan, "Batalkan & Downgrade")) return;
        try {
            us.batalkanDanDowngrade(u, LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Transaksi " + u.getId() + " dibatalkan. Tier member "
                    + u.getPelanggan().getNama() + " diturunkan kembali ke " + u.getDariTier() + ".");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    // ================= REFRESH DATA =================
    private void refreshBooking() {
        List<Booking> data = new ArrayList<>();
        Object f = cbFilter.getSelectedItem();
        for (Booking b : bs.getSemuaBooking())
            if (f == null || SEMUA.equals(f) || b.getStatus() == f) data.add(b);
        Collections.reverse(data);
        UiUtil.isiTabelBooking(tBooking, mBooking, data, true, LocalDateTime.now());
    }

    private void refreshUpgrade() {
        List<UpgradeMember> data = new ArrayList<>();
        Object f = cbFilterUpgrade.getSelectedItem();
        for (UpgradeMember u : us.getSemuaPengajuan())
            if (f == null || SEMUA.equals(f) || u.getStatus() == f) data.add(u);
        Collections.reverse(data);
        UiUtil.isiTabelUpgrade(tUpgrade, mUpgrade, data, true);
    }

    private void refreshLapangan() {
        Object[][] baris = new Object[bs.getLapangan().size()][];
        int i = 0;
        for (Lapangan l : bs.getLapangan()) {
            baris[i++] = new Object[]{
                    l.getId(), l.getNama(), l.getJenis(),
                    Fmt.rp(l.getTarifDasar()) + "/jam",
                    Fmt.rp(l.getBiayaTambahan()),
                    l.getKeterangan()
            };
        }
        mLapangan.setDataVector(baris, new String[]{"ID", "Nama Lapangan", "Jenis", "Tarif Dasar", "Biaya Tambahan", "Keterangan"});
        tLapangan.getColumnModel().getColumn(0).setPreferredWidth(72);
        tLapangan.getColumnModel().getColumn(1).setPreferredWidth(180);
        tLapangan.getColumnModel().getColumn(2).setCellRenderer(new UiUtil.PillBadgeRenderer());
        tLapangan.getColumnModel().getColumn(2).setPreferredWidth(135);
        tLapangan.getColumnModel().getColumn(3).setCellRenderer(new UiUtil.ModernCellRenderer(SwingConstants.RIGHT, true));
        tLapangan.getColumnModel().getColumn(3).setPreferredWidth(135);
        tLapangan.getColumnModel().getColumn(4).setCellRenderer(new UiUtil.ModernCellRenderer(SwingConstants.RIGHT, false));
        tLapangan.getColumnModel().getColumn(4).setPreferredWidth(125);
        tLapangan.getColumnModel().getColumn(5).setPreferredWidth(260);
    }

    private void refreshLaporan() {
        Map<StatusBooking, Long> perStatusBooking = bs.jumlahPerStatus();
        Map<JenisLapangan, Double> perJenis = bs.pendapatanPerJenis();
        Map<StatusUpgrade, Long> perStatusUpgrade = us.jumlahPerStatus();

        double totalBooking = bs.totalPendapatan();
        double totalUpgrade = us.totalPendapatan();
        double totalGabungan = totalBooking + totalUpgrade;

        long bookingSukses = perStatusBooking.getOrDefault(StatusBooking.LUNAS, 0L)
                + perStatusBooking.getOrDefault(StatusBooking.SELESAI, 0L);
        long upgradeSukses = perStatusUpgrade.getOrDefault(StatusUpgrade.LUNAS, 0L);
        long menungguTotal = perStatusBooking.getOrDefault(StatusBooking.MENUNGGU_KONFIRMASI, 0L)
                + perStatusUpgrade.getOrDefault(StatusUpgrade.MENUNGGU_KONFIRMASI, 0L);

        kpiTotal.setValue(Fmt.rp(totalGabungan));
        kpiTotal.setSubtitle("Booking + Upgrade Lunas");

        kpiBooking.setValue(Fmt.rp(totalBooking));
        kpiBooking.setSubtitle(bs.getSemuaBooking().size() + " total reservasi");

        kpiUpgrade.setValue(Fmt.rp(totalUpgrade));
        kpiUpgrade.setSubtitle(upgradeSukses + " upgrade member lunas");

        kpiTransaksi.setValue((bookingSukses + upgradeSukses) + " transaksi");
        kpiTransaksi.setSubtitle(menungguTotal + " menunggu konfirmasi admin");

        // Breakdown pendapatan per jenis lapangan dengan progress bar halus
        panelPerJenis.removeAll();
        double maksJenis = 1.0;
        for (Double v : perJenis.values()) if (v != null && v > maksJenis) maksJenis = v;

        for (Map.Entry<JenisLapangan, Double> e : perJenis.entrySet()) {
            double val = e.getValue() == null ? 0.0 : e.getValue();
            double ratio = Math.min(1.0, val / maksJenis);
            panelPerJenis.add(createJenisRow(e.getKey(), val, ratio));
            panelPerJenis.add(Box.createVerticalStrut(10));
        }
        panelPerJenis.add(Box.createVerticalGlue());
        lblSubtotalJenis.setText(Fmt.rp(totalBooking));
        panelPerJenis.revalidate();
        panelPerJenis.repaint();

        // Breakdown jumlah booking & upgrade per status
        panelPerStatus.removeAll();
        panelPerStatus.add(createSectionSubHeader("Status Booking"));
        panelPerStatus.add(Box.createVerticalStrut(8));
        for (Map.Entry<StatusBooking, Long> e : perStatusBooking.entrySet()) {
            panelPerStatus.add(createStatusCountRow(createBookingStatusPill(e.getKey()), e.getValue() + " booking"));
            panelPerStatus.add(Box.createVerticalStrut(7));
        }

        panelPerStatus.add(Box.createVerticalStrut(12));
        panelPerStatus.add(createSectionSubHeader("Status Upgrade Member"));
        panelPerStatus.add(Box.createVerticalStrut(8));
        for (Map.Entry<StatusUpgrade, Long> e : perStatusUpgrade.entrySet()) {
            panelPerStatus.add(createStatusCountRow(createUpgradeStatusPill(e.getKey()), e.getValue() + " transaksi"));
            panelPerStatus.add(Box.createVerticalStrut(7));
        }
        panelPerStatus.add(Box.createVerticalGlue());
        panelPerStatus.revalidate();
        panelPerStatus.repaint();
    }

    private JPanel createJenisRow(JenisLapangan jenis, double nominal, double ratio) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel name = new JLabel(jenis.toString());
        name.setFont(UiUtil.fontLabel(13f));
        name.setForeground(UiUtil.TEXT_HEADING);
        JLabel amount = new JLabel(Fmt.rp(nominal));
        amount.setFont(UiUtil.fontLabel(13f));
        amount.setForeground(nominal > 0 ? UiUtil.PRIMARY_END : UiUtil.TEXT_MUTED);
        top.add(name, BorderLayout.WEST);
        top.add(amount, BorderLayout.EAST);

        JComponent bar = new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(0, 8); }
            @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 8); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtil.enableAntiAliasing(g2);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(UiUtil.SURFACE_SUBTLE);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
                if (ratio > 0) {
                    int fillW = Math.max(h, (int) Math.round(w * ratio));
                    g2.setPaint(new GradientPaint(0, 0, UiUtil.PRIMARY_START, fillW, 0, UiUtil.PRIMARY_END));
                    g2.fill(new RoundRectangle2D.Double(0, 0, fillW, h, h, h));
                }
                g2.dispose();
            }
        };

        box.add(top);
        box.add(Box.createVerticalStrut(6));
        box.add(bar);
        return box;
    }

    private JLabel createSectionSubHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiUtil.fontLabel(12.5f));
        lbl.setForeground(UiUtil.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel createStatusCountRow(UiUtil.PillLabel pill, String countText) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setBorder(new EmptyBorder(2, 0, 2, 2));

        JLabel cnt = new JLabel(countText);
        cnt.setFont(UiUtil.fontLabel(13f));
        cnt.setForeground(UiUtil.TEXT_HEADING);

        JPanel leftWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftWrap.setOpaque(false);
        leftWrap.add(pill);

        row.add(leftWrap, BorderLayout.WEST);
        row.add(cnt, BorderLayout.EAST);
        return row;
    }

    private UiUtil.PillLabel createBookingStatusPill(StatusBooking s) {
        switch (s) {
            case LUNAS:
            case SELESAI:
                return new UiUtil.PillLabel(s.toString(), UiUtil.BADGE_GREEN_BG, UiUtil.BADGE_GREEN_ACCENT, UiUtil.BADGE_GREEN_TEXT, true);
            case DIBATALKAN:
                return new UiUtil.PillLabel(s.toString(), UiUtil.BADGE_RED_BG, UiUtil.BADGE_RED_ACCENT, UiUtil.BADGE_RED_TEXT, true);
            default:
                return new UiUtil.PillLabel(s.toString(), UiUtil.BADGE_AMBER_BG, UiUtil.BADGE_AMBER_ACCENT, UiUtil.BADGE_AMBER_TEXT, true);
        }
    }

    private UiUtil.PillLabel createUpgradeStatusPill(StatusUpgrade s) {
        switch (s) {
            case LUNAS:
                return new UiUtil.PillLabel(s.toString(), UiUtil.BADGE_GREEN_BG, UiUtil.BADGE_GREEN_ACCENT, UiUtil.BADGE_GREEN_TEXT, true);
            case DIBATALKAN:
                return new UiUtil.PillLabel(s.toString(), UiUtil.BADGE_RED_BG, UiUtil.BADGE_RED_ACCENT, UiUtil.BADGE_RED_TEXT, true);
            default:
                return new UiUtil.PillLabel(s.toString(), UiUtil.BADGE_AMBER_BG, UiUtil.BADGE_AMBER_ACCENT, UiUtil.BADGE_AMBER_TEXT, true);
        }
    }

    private void refreshSemua() {
        bs.perbaruiStatus(LocalDateTime.now());
        refreshBooking();
        refreshUpgrade();
        refreshLapangan();
        refreshLaporan();
    }
}

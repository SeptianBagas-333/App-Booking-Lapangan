package view;

import exception.*;
import model.*;
import service.AuthService;
import service.BookingService;
import service.UpgradeMemberService;
import util.Fmt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Layar utama pelanggan dengan sidebar navigasi modern, jadwal pastel, riwayat, dan upgrade member. */
public class PelangganFrame extends JFrame {
    private final Pelanggan user;
    private final BookingService bs;
    private final UpgradeMemberService us;
    private final AuthService auth;
    private final Timer timer;

    private final UiUtil.TablessCardPane pages = new UiUtil.TablessCardPane();
    private final List<UiUtil.SidebarNavButton> navButtons = new ArrayList<>();
    private final UiUtil.AppSidebar sidebar;

    // Halaman Jadwal & Booking
    private final JSpinner spTanggal = new JSpinner(new SpinnerDateModel());
    private final JComboBox<JenisLapangan> cbJenis = new JComboBox<>(JenisLapangan.values());
    private final DefaultTableModel mJadwal = UiUtil.modelTabel();
    private final JTable tJadwal = UiUtil.tabel(mJadwal);
    private final UiUtil.PastelSlotRenderer slotRenderer = new UiUtil.PastelSlotRenderer();
    private List<Lapangan> kolomLapangan;

    // Halaman Riwayat Pemesanan
    private final DefaultTableModel mRiwayat = UiUtil.modelTabel();
    private final JTable tRiwayat = UiUtil.tabel(mRiwayat);

    // Halaman Upgrade Member
    private final UiUtil.PillLabel badgeTierCard;
    private final JLabel lblDiskonMember = new JLabel();
    private final JLabel lblStatusUpgrade = new JLabel();
    private final JComboBox<PaketUpgrade> cbPaketUpgrade = new JComboBox<>();
    private final JComboBox<String> cbMetodeUpgrade = new JComboBox<>(Pembayaran.DAFTAR_METODE);
    private final UiUtil.ModernButton btnAjukanUpgrade =
            new UiUtil.ModernButton("Ajukan Upgrade", UiUtil.ModernButton.Variant.PRIMARY);
    private final DefaultTableModel mUpgrade = UiUtil.modelTabel();
    private final JTable tUpgrade = UiUtil.tabel(mUpgrade);

    public PelangganFrame(Pelanggan user, BookingService bs, UpgradeMemberService us, AuthService auth) {
        super("ArenaBook - Pelanggan (" + user.getNama() + ")");
        this.user = user;
        this.bs = bs;
        this.us = us;
        this.auth = auth;
        this.badgeTierCard = UiUtil.createMemberBadge(user.getMember());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);

        // Sidebar kiri terang
        sidebar = new UiUtil.AppSidebar(
                "Portal Pelanggan",
                user.getNama(),
                user.getDiskon().getNama(),
                UiUtil.createMemberBadge(user.getMember()),
                this::logout
        );

        UiUtil.SidebarNavButton navJadwal = new UiUtil.SidebarNavButton("Jadwal & Booking", UiUtil.NavIcon.CALENDAR);
        UiUtil.SidebarNavButton navRiwayat = new UiUtil.SidebarNavButton("Riwayat Pemesanan", UiUtil.NavIcon.LIST);
        UiUtil.SidebarNavButton navUpgrade = new UiUtil.SidebarNavButton("Upgrade Member", UiUtil.NavIcon.CROWN);
        navButtons.add(navJadwal);
        navButtons.add(navRiwayat);
        navButtons.add(navUpgrade);

        for (int i = 0; i < navButtons.size(); i++) {
            final int idx = i;
            UiUtil.SidebarNavButton btn = navButtons.get(i);
            btn.addActionListener(e -> pages.setSelectedIndex(idx));
            sidebar.addNavButton(btn);
        }

        // Halaman konten utama (TablessCardPane di atas GradientCanvasPanel)
        pages.addTab("Jadwal & Booking", buatHalamanJadwal());
        pages.addTab("Riwayat Pemesanan", buatHalamanRiwayat());
        pages.addTab("Upgrade Member", buatHalamanUpgrade());
        pages.addChangeListener(e -> {
            syncSidebarSelection();
            refreshSemua();
        });

        UiUtil.GradientCanvasPanel contentHost =
                new UiUtil.GradientCanvasPanel(UiUtil.BG_CANVAS_START, UiUtil.BG_CANVAS_END, true);
        contentHost.setLayout(new BorderLayout());
        contentHost.setBorder(new EmptyBorder(22, 26, 22, 26));
        contentHost.add(pages, BorderLayout.CENTER);

        JPanel shell = new JPanel(new BorderLayout());
        shell.add(sidebar, BorderLayout.WEST);
        shell.add(contentHost, BorderLayout.CENTER);
        setContentPane(shell);

        syncSidebarSelection();
        refreshSemua();

        timer = new Timer(30_000, e -> refreshSemua());
        timer.start();
    }

    private void syncSidebarSelection() {
        int sel = pages.getSelectedIndex();
        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setActive(i == sel);
        }
    }

    private void logout() {
        timer.stop();
        dispose();
        UiUtil.tampilkanMaksimal(new LoginFrame(auth, bs, us));
    }

    private void perbaruiInfoMember() {
        UiUtil.updateMemberBadge(sidebar.getRoleBadge(), user.getMember());
        sidebar.setAccountSubtitle(user.getDiskon().getNama());
        UiUtil.updateMemberBadge(badgeTierCard, user.getMember());
        lblDiskonMember.setText("Benefit aktif: " + user.getDiskon().getNama());
    }

    // ================= HALAMAN 1: JADWAL & BOOKING =================
    private JPanel buatHalamanJadwal() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        JPanel header = UiUtil.createPageHeader(
                "Jadwal & Booking",
                "Pilih slot kosong berwarna hijau untuk membuat reservasi lapangan.",
                UiUtil.createAutoRefreshBadge()
        );

        // Toolbar filter dalam ShadowCardPanel
        spTanggal.setEditor(new JSpinner.DateEditor(spTanggal, "dd-MM-yyyy"));
        spTanggal.setValue(new Date());
        UiUtil.styleDateSpinner(spTanggal);
        UiUtil.styleComboBox(cbJenis);

        UiUtil.ModernButton prev = new UiUtil.ModernButton("<", UiUtil.ModernButton.Variant.SECONDARY, 9);
        UiUtil.ModernButton next = new UiUtil.ModernButton(">", UiUtil.ModernButton.Variant.SECONDARY, 9);
        prev.setPreferredSize(new Dimension(40, 38));
        next.setPreferredSize(new Dimension(40, 38));
        prev.setBorder(new EmptyBorder(0, 0, 0, 0));
        next.setBorder(new EmptyBorder(0, 0, 0, 0));

        UiUtil.ModernButton btnRefresh = new UiUtil.ModernButton("Refresh", UiUtil.ModernButton.Variant.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(96, 38));
        UiUtil.ModernButton btnCariLintas = new UiUtil.ModernButton("Cari Semua Lapangan", UiUtil.ModernButton.Variant.PRIMARY);
        
        prev.addActionListener(e -> geserTanggal(-1));
        next.addActionListener(e -> geserTanggal(1));
        spTanggal.addChangeListener(e -> refreshJadwal());
        cbJenis.addActionListener(e -> refreshJadwal());
        btnRefresh.addActionListener(e -> refreshSemua());
        btnCariLintas.addActionListener(e -> cariSlotLintasLapangan());

        UiUtil.ShadowCardPanel filterCard = new UiUtil.ShadowCardPanel(14, 6);
        filterCard.setLayout(new BorderLayout(12, 0));
        filterCard.setContentPadding(10, 14, 10, 14);

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterLeft.setOpaque(false);
        JLabel lblTgl = new JLabel("Tanggal:");
        lblTgl.setFont(UiUtil.fontLabel(13f));
        lblTgl.setForeground(UiUtil.TEXT_BODY);
        JLabel lblSport = new JLabel("Olahraga:");
        lblSport.setFont(UiUtil.fontLabel(13f));
        lblSport.setForeground(UiUtil.TEXT_BODY);

        filterLeft.add(lblTgl);
        filterLeft.add(prev);
        filterLeft.add(spTanggal);
        filterLeft.add(next);
        filterLeft.add(Box.createHorizontalStrut(14));
        filterLeft.add(lblSport);
        filterLeft.add(cbJenis);

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        filterRight.setOpaque(false);
        filterRight.add(btnCariLintas);
        filterRight.add(btnRefresh);

        filterCard.add(filterLeft, BorderLayout.WEST);
        filterCard.add(filterRight, BorderLayout.EAST);

        // Tabel jadwal pastel dalam ShadowCardPanel
        tJadwal.setRowHeight(38);
        tJadwal.setCellSelectionEnabled(true);
        tJadwal.setRowSelectionAllowed(false);
        tJadwal.setShowHorizontalLines(true);
        tJadwal.setShowVerticalLines(false);
        tJadwal.setDefaultRenderer(Object.class, slotRenderer);
        tJadwal.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int r = tJadwal.rowAtPoint(e.getPoint());
                int c = tJadwal.columnAtPoint(e.getPoint());
                slotRenderer.setHoveredCell(r, c);
                boolean clickable = r >= 0 && c >= 1 && "Kosong".equals(mJadwal.getValueAt(r, c));
                tJadwal.setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                tJadwal.repaint();
            }
        });
        tJadwal.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                slotRenderer.setHoveredCell(-1, -1);
                tJadwal.setCursor(Cursor.getDefaultCursor());
                tJadwal.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) { klikSel(); }
        });

        JScrollPane spJadwal = new JScrollPane(tJadwal);
        UiUtil.styleScrollPane(spJadwal);

        // Legenda warna berbentuk chip rapi + catatan tarif
        JPanel legenda = new JPanel(new BorderLayout(10, 0));
        legenda.setOpaque(false);
        legenda.setBorder(new EmptyBorder(10, 2, 0, 2));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        chips.setOpaque(false);
        chips.add(new UiUtil.PillLabel("Kosong (klik untuk booking)", UiUtil.BADGE_GREEN_BG, UiUtil.BADGE_GREEN_ACCENT, UiUtil.BADGE_GREEN_TEXT, true));
        chips.add(new UiUtil.PillLabel("Terisi", UiUtil.BADGE_RED_BG, UiUtil.BADGE_RED_ACCENT, UiUtil.BADGE_RED_TEXT, true));
        chips.add(new UiUtil.PillLabel("Booking Anda", UiUtil.BADGE_BLUE_BG, UiUtil.BADGE_BLUE_ACCENT, UiUtil.BADGE_BLUE_TEXT, true));
        chips.add(new UiUtil.PillLabel("Tidak Tersedia", UiUtil.BADGE_GRAY_BG, UiUtil.BADGE_GRAY_ACCENT, UiUtil.BADGE_GRAY_TEXT, true));

        JLabel tarifNote = new JLabel("Jam sibuk 17.00-22.00 (x1,25)  |  Sabtu-Minggu (x1,15)");
        tarifNote.setFont(UiUtil.fontBody(12f));
        tarifNote.setForeground(UiUtil.TEXT_MUTED);
        JPanel noteWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        noteWrap.setOpaque(false);
        noteWrap.add(tarifNote);

        legenda.add(chips, BorderLayout.WEST);
        legenda.add(noteWrap, BorderLayout.EAST);

        UiUtil.ShadowCardPanel tableCard = new UiUtil.ShadowCardPanel(16, 7);
        tableCard.setLayout(new BorderLayout(0, 4));
        tableCard.setContentPadding(14, 14, 12, 14);
        tableCard.add(spJadwal, BorderLayout.CENTER);
        tableCard.add(legenda, BorderLayout.SOUTH);

        JPanel topStack = new JPanel(new BorderLayout(0, 12));
        topStack.setOpaque(false);
        topStack.add(header, BorderLayout.NORTH);
        topStack.add(filterCard, BorderLayout.SOUTH);

        root.add(topStack, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
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
        tJadwal.getColumnModel().getColumn(0).setPreferredWidth(128);
        tJadwal.getColumnModel().getColumn(0).setMaxWidth(150);
    }

    private void cariSlotLintasLapangan() {
        JSpinner spCariTanggal = new JSpinner(new SpinnerDateModel(
                Date.from(tanggalDipilih().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH));
        spCariTanggal.setEditor(new JSpinner.DateEditor(spCariTanggal, "dd-MM-yyyy"));
        UiUtil.styleDateSpinner(spCariTanggal);

        Integer[] jam = new Integer[BookingService.JAM_TUTUP - BookingService.JAM_BUKA];
        for (int i = 0; i < jam.length; i++) jam[i] = BookingService.JAM_BUKA + i;

        JComboBox<Integer> cbJam = new JComboBox<>(jam);
        JComboBox<Integer> cbDurasi = new JComboBox<>(new Integer[]{1, 2, 3});
        UiUtil.styleComboBox(cbJam);
        UiUtil.styleComboBox(cbDurasi);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JComponent widthGuide = new Box.Filler(new Dimension(390, 0), new Dimension(390, 0), new Dimension(390, 0));
        content.add(widthGuide);
        content.add(createFormRow("Tanggal", spCariTanggal));
        content.add(Box.createVerticalStrut(10));
        content.add(createFormRow("Jam mulai", cbJam));
        content.add(Box.createVerticalStrut(10));
        content.add(createFormRow("Durasi", cbDurasi));

        boolean ok = UiUtil.showFormDialog(
                this,
                "Cari Slot Lintas Lapangan",
                "Temukan semua lapangan yang tersedia pada tanggal dan jam yang dipilih.",
                content,
                "Cari Slot",
                UiUtil.ModernButton.Variant.PRIMARY
        );
        if (!ok) return;

        LocalDate tanggal = ((Date) spCariTanggal.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime mulai = tanggal.atTime((Integer) cbJam.getSelectedItem(), 0);
        int durasi = (Integer) cbDurasi.getSelectedItem();

        try {
            List<Lapangan> tersedia = bs.cariLapanganKosong(mulai, durasi, null, LocalDateTime.now());
            DefaultTableModel hasilModel = new DefaultTableModel(
                    new Object[]{"Lapangan", "Jenis", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            for (Lapangan l : tersedia) hasilModel.addRow(new Object[]{l.getNama(), l.getJenis(), "Tersedia"});

            JTable hasilTabel = UiUtil.tabel(hasilModel);
            hasilTabel.setRowHeight(34);
            hasilTabel.setAutoCreateRowSorter(true);
            JScrollPane scroll = new JScrollPane(hasilTabel);
            UiUtil.styleScrollPane(scroll);
            scroll.setPreferredSize(new Dimension(520, Math.min(360, 70 + Math.max(1, tersedia.size()) * 36)));

            String subjudul = tersedia.isEmpty()
                    ? "Tidak ada lapangan kosong pada waktu tersebut."
                    : tersedia.size() + " lapangan tersedia untuk " + mulai.format(Fmt.TANGGAL) + " " + mulai.format(Fmt.JAM);
            UiUtil.showPreviewDialog(this, "Hasil Pencarian Slot", subjudul, scroll);
        } catch (IllegalArgumentException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private JPanel createFormRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel caption = new JLabel(label);
        caption.setFont(UiUtil.fontLabel(13f));
        caption.setForeground(UiUtil.TEXT_BODY);
        caption.setPreferredSize(new Dimension(105, 36));
        row.add(caption, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
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
        us.segarkanPelanggan(user);
        perbaruiInfoMember();

        int maks = 0;
        for (int d = 1; d <= BookingService.MAKS_DURASI; d++) {
            if (mulai.getHour() + d > BookingService.JAM_TUTUP) break;
            if (!bs.isSlotTersedia(l, mulai, d)) break;
            maks = d;
        }
        if (maks == 0) {
            UiUtil.error(this, "Slot sudah tidak tersedia.");
            refreshSemua();
            return;
        }

        Integer[] pilihan = new Integer[maks];
        for (int i = 0; i < maks; i++) pilihan[i] = i + 1;
        JComboBox<Integer> cbDurasi = new JComboBox<>(pilihan);
        UiUtil.styleComboBox(cbDurasi);
        cbDurasi.setPreferredSize(new Dimension(110, 36));

        JLabel valJam = new JLabel();
        JLabel valSewa = new JLabel();
        JLabel valDiskon = new JLabel();
        JLabel valTotal = new JLabel();
        valTotal.setFont(UiUtil.fontHeading(17f));
        valTotal.setForeground(UiUtil.PRIMARY_END);

        Runnable update = () -> {
            int d = (Integer) cbDurasi.getSelectedItem();
            BookingService.Rincian r = bs.hitungRincian(l, mulai, d, user);
            valJam.setText(mulai.format(Fmt.JAM) + " - " + mulai.plusHours(d).format(Fmt.JAM) + " (" + d + " jam)");
            valSewa.setText(Fmt.rp(r.sewa));
            valDiskon.setText("-" + Fmt.rp(r.diskon) + "  (" + r.namaDiskon + ")");
            valTotal.setText(Fmt.rp(r.total));
        };
        cbDurasi.addActionListener(e -> update.run());
        update.run();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JComponent widthGuide = new Box.Filler(new Dimension(430, 0), new Dimension(430, 0), new Dimension(430, 0));
        widthGuide.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(widthGuide);

        JPanel durasiRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        durasiRow.setOpaque(false);
        durasiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblDurasi = new JLabel("Durasi Bermain (jam):");
        lblDurasi.setFont(UiUtil.fontLabel(13f));
        lblDurasi.setForeground(UiUtil.TEXT_BODY);
        durasiRow.add(lblDurasi);
        durasiRow.add(cbDurasi);

        JPanel summaryBox = new JPanel(new GridBagLayout());
        summaryBox.setBackground(UiUtil.SURFACE_SIDEBAR);
        summaryBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtil.BORDER_SUBTLE, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        summaryBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        addSummaryRow(summaryBox, 0, "Lapangan", l.getNama() + " (" + l.getJenis() + ")", false);
        addSummaryRow(summaryBox, 1, "Tanggal", mulai.format(Fmt.TANGGAL), false);
        addSummaryRow(summaryBox, 2, "Waktu", valJam, false);
        addSummaryRow(summaryBox, 3, "Biaya Sewa", valSewa, false);
        addSummaryRow(summaryBox, 4, "Diskon", valDiskon, false);
        addSummaryRow(summaryBox, 5, "Total Bayar", valTotal, true);

        content.add(durasiRow);
        if (!l.getKeterangan().isEmpty()) {
            JLabel ket = new JLabel("Catatan lapangan: " + l.getKeterangan());
            ket.setFont(UiUtil.fontBody(12f));
            ket.setForeground(UiUtil.TEXT_MUTED);
            ket.setBorder(new EmptyBorder(6, 10, 0, 0));
            ket.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(ket);
        }
        content.add(Box.createVerticalStrut(12));
        content.add(summaryBox);

        boolean ok = UiUtil.showFormDialog(
                this,
                "Buat Booking Lapangan",
                "Periksa estimasi biaya sebelum mengonfirmasi reservasi.",
                content,
                "Buat Booking",
                UiUtil.ModernButton.Variant.PRIMARY
        );
        if (!ok) return;

        try {
            Booking b = bs.buatBooking(user, l, mulai, (Integer) cbDurasi.getSelectedItem(), LocalDateTime.now());
            refreshSemua();
            if (UiUtil.konfirmasi(this, "Booking " + b.getId() + " berhasil dibuat (Menunggu Bayar).\nBayar sekarang? "
                    + "Batas pembayaran pukul " + b.getBatasBayar().format(Fmt.JAM) + ".")) {
                dialogBayar(b);
            }
        } catch (JadwalBentrokException | IllegalArgumentException ex) {
            UiUtil.error(this, ex.getMessage());
            refreshSemua();
        }
    }

    private void addSummaryRow(JPanel box, int row, String label, Object value, boolean highlight) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(highlight ? 8 : 3, 0, 3, 12);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.weightx = 0.4;
        JLabel l = new JLabel(label);
        l.setFont(highlight ? UiUtil.fontLabel(13.5f) : UiUtil.fontBody(13f));
        l.setForeground(highlight ? UiUtil.TEXT_HEADING : UiUtil.TEXT_MUTED);
        box.add(l, g);

        g.gridx = 1;
        g.weightx = 0.6;
        g.anchor = GridBagConstraints.EAST;
        g.insets = new Insets(highlight ? 8 : 3, 0, 3, 0);
        JLabel v = (value instanceof JLabel) ? (JLabel) value : new JLabel(String.valueOf(value));
        if (!highlight) {
            v.setFont(UiUtil.fontLabel(13f));
            v.setForeground(UiUtil.TEXT_HEADING);
        }
        box.add(v, g);
    }

    // ================= HALAMAN 2: RIWAYAT PEMESANAN =================
    private JPanel buatHalamanRiwayat() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        JPanel header = UiUtil.createPageHeader(
                "Riwayat Pemesanan",
                "Pantau status booking, selesaikan pembayaran, atau cetak struk resmi.",
                UiUtil.createAutoRefreshBadge()
        );

        UiUtil.ModernButton btnBayar = new UiUtil.ModernButton("Bayar", UiUtil.ModernButton.Variant.PRIMARY);
        UiUtil.ModernButton btnStruk = new UiUtil.ModernButton("Cetak Struk", UiUtil.ModernButton.Variant.SECONDARY);
        UiUtil.ModernButton btnBatal = new UiUtil.ModernButton("Batalkan", UiUtil.ModernButton.Variant.DANGER_SOFT);
        UiUtil.ModernButton btnReschedule = new UiUtil.ModernButton("Ubah Jadwal", UiUtil.ModernButton.Variant.SECONDARY);
        UiUtil.ModernButton btnRefresh = new UiUtil.ModernButton("Refresh", UiUtil.ModernButton.Variant.SECONDARY);

        btnBayar.addActionListener(e -> aksiBayar());
        btnStruk.addActionListener(e -> aksiStruk());
        btnBatal.addActionListener(e -> aksiBatal());
        btnReschedule.addActionListener(e -> aksiReschedule());
        btnRefresh.addActionListener(e -> refreshSemua());

        UiUtil.ShadowCardPanel actionCard = new UiUtil.ShadowCardPanel(14, 6);
        actionCard.setLayout(new BorderLayout());
        actionCard.setContentPadding(10, 14, 10, 14);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        leftActions.add(btnBayar);
        leftActions.add(btnStruk);
        leftActions.add(btnBatal);
        leftActions.add(btnReschedule);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        rightActions.add(btnRefresh);

        actionCard.add(leftActions, BorderLayout.WEST);
        actionCard.add(rightActions, BorderLayout.EAST);

        JScrollPane spRiwayat = new JScrollPane(tRiwayat);
        UiUtil.styleScrollPane(spRiwayat);

        UiUtil.ShadowCardPanel tableCard = new UiUtil.ShadowCardPanel(16, 7);
        tableCard.setLayout(new BorderLayout());
        tableCard.setContentPadding(14, 14, 14, 14);
        tableCard.add(spRiwayat, BorderLayout.CENTER);

        JPanel topStack = new JPanel(new BorderLayout(0, 12));
        topStack.setOpaque(false);
        topStack.add(header, BorderLayout.NORTH);
        topStack.add(actionCard, BorderLayout.SOUTH);

        root.add(topStack, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
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
        JComboBox<String> cbMetode = new JComboBox<>(Pembayaran.DAFTAR_METODE);
        UiUtil.styleComboBox(cbMetode);
        cbMetode.setPreferredSize(new Dimension(200, 38));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JComponent widthGuide = new Box.Filler(new Dimension(430, 0), new Dimension(430, 0), new Dimension(430, 0));
        widthGuide.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(widthGuide);

        JPanel infoBox = new JPanel(new GridBagLayout());
        infoBox.setBackground(UiUtil.SURFACE_SIDEBAR);
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtil.BORDER_SUBTLE, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        addSummaryRow(infoBox, 0, "ID Booking", b.getId(), false);
        addSummaryRow(infoBox, 1, "Lapangan", b.getLapangan().getNama(), false);
        addSummaryRow(infoBox, 2, "Jadwal", b.getMulai().format(Fmt.TANGGAL) + "  " + b.getMulai().format(Fmt.JAM) + "-" + b.getSelesai().format(Fmt.JAM), false);
        addSummaryRow(infoBox, 3, "Diskon", "-" + Fmt.rp(b.getDiskon()), false);
        JLabel lblTot = new JLabel(Fmt.rp(b.getTotal()));
        lblTot.setFont(UiUtil.fontHeading(16f));
        lblTot.setForeground(UiUtil.PRIMARY_END);
        addSummaryRow(infoBox, 4, "Total Bayar", lblTot, true);

        JPanel metodeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metodeRow.setOpaque(false);
        metodeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblMetode = new JLabel("Metode Pembayaran:");
        lblMetode.setFont(UiUtil.fontLabel(13f));
        lblMetode.setForeground(UiUtil.TEXT_BODY);
        metodeRow.add(lblMetode);
        metodeRow.add(cbMetode);

        JLabel note = new JLabel("E-Wallet langsung lunas. Transfer/Tunai menunggu konfirmasi admin.");
        note.setFont(UiUtil.fontBody(12f));
        note.setForeground(UiUtil.TEXT_MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(infoBox);
        content.add(Box.createVerticalStrut(12));
        content.add(metodeRow);
        content.add(Box.createVerticalStrut(8));
        content.add(note);

        boolean ok = UiUtil.showFormDialog(
                this,
                "Pembayaran Booking",
                "Pilih metode pembayaran untuk menyelesaikan pesanan.",
                content,
                "Bayar Sekarang",
                UiUtil.ModernButton.Variant.PRIMARY
        );
        if (!ok) return;

        try {
            bs.bayar(b, Pembayaran.dari((String) cbMetode.getSelectedItem()), LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, b.getStatus() == StatusBooking.LUNAS
                    ? "Pembayaran berhasil. Booking berstatus LUNAS."
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
        String pesan = "Batalkan booking " + b.getId() + " (" + b.getLapangan().getNama() + ")?";
        if (b.getStatus() == StatusBooking.LUNAS) {
            pesan += "\n\nEstimasi refund: " + Fmt.rp(b.hitungRefund(now))
                    + "\n(>=24 jam: 100%, 3-24 jam: 50%, <3 jam: 0%)";
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

    private void aksiReschedule() {
        Booking b = bookingTerpilih();
        if (b == null) return;

        JSpinner spTanggalBaru = new JSpinner(new SpinnerDateModel(
                Date.from(b.getMulai().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH));
        spTanggalBaru.setEditor(new JSpinner.DateEditor(spTanggalBaru, "dd-MM-yyyy"));
        UiUtil.styleDateSpinner(spTanggalBaru);

        JSpinner spJam = new JSpinner(new SpinnerNumberModel(b.getMulai().getHour(), 8, 21, 1));
        JSpinner spDurasi = new JSpinner(new SpinnerNumberModel(b.getDurasi(), 1, BookingService.MAKS_DURASI, 1));
        spJam.setPreferredSize(new Dimension(110, 36));
        spDurasi.setPreferredSize(new Dimension(110, 36));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JComponent widthGuide = new Box.Filler(new Dimension(390, 0), new Dimension(390, 0), new Dimension(390, 0));
        content.add(widthGuide);
        content.add(createFormRow("Tanggal baru", spTanggalBaru));
        content.add(Box.createVerticalStrut(10));
        content.add(createFormRow("Jam mulai", spJam));
        content.add(Box.createVerticalStrut(10));
        content.add(createFormRow("Durasi", spDurasi));
        JLabel note = new JLabel("Reschedule hanya tersedia minimal 3 jam sebelum jadwal lama.");
        note.setFont(UiUtil.fontBody(12f));
        note.setForeground(UiUtil.TEXT_MUTED);
        note.setBorder(new EmptyBorder(12, 0, 0, 0));
        content.add(note);

        boolean ok = UiUtil.showFormDialog(
                this,
                "Ubah Jadwal Booking",
                "Jadwal lama: " + b.getMulai().format(Fmt.TANGGAL) + " " + b.getMulai().format(Fmt.JAM),
                content,
                "Simpan Jadwal",
                UiUtil.ModernButton.Variant.PRIMARY
        );
        if (!ok) return;

        LocalDate tanggalBaru = ((Date) spTanggalBaru.getValue()).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime mulaiBaru = tanggalBaru.atTime((Integer) spJam.getValue(), 0);

        try {
            bs.reschedule(b, mulaiBaru, (Integer) spDurasi.getValue(), LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, "Jadwal booking berhasil diubah.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
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
            ta.setForeground(UiUtil.TEXT_HEADING);
            ta.setBackground(UiUtil.SURFACE_SIDEBAR);
            ta.setEditable(false);
            ta.setBorder(new EmptyBorder(14, 16, 14, 16));

            JScrollPane sp = new JScrollPane(ta);
            UiUtil.styleScrollPane(sp);
            sp.setPreferredSize(new Dimension(450, 320));

            UiUtil.showPreviewDialog(this, "Pratinjau Struk Booking", "Tersimpan di: " + f, sp);
        } catch (IllegalStateException | IOException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    // ================= HALAMAN 3: UPGRADE MEMBER =================
    private JPanel buatHalamanUpgrade() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setOpaque(false);

        JPanel header = UiUtil.createPageHeader(
                "Upgrade Member",
                "Tingkatkan tier member untuk menikmati diskon otomatis pada setiap booking.",
                UiUtil.createAutoRefreshBadge()
        );

        // Kartu ringkasan tier member saat ini
        UiUtil.ShadowCardPanel statusCard = new UiUtil.ShadowCardPanel(16, 6);
        statusCard.setLayout(new BorderLayout(16, 6));
        statusCard.setContentPadding(16, 20, 16, 20);

        JPanel statusLeft = new JPanel();
        statusLeft.setOpaque(false);
        statusLeft.setLayout(new BoxLayout(statusLeft, BoxLayout.Y_AXIS));

        JPanel tierRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tierRow.setOpaque(false);
        tierRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblCap = new JLabel("Tier Member Saat Ini:");
        lblCap.setFont(UiUtil.fontHeading(16f));
        lblCap.setForeground(UiUtil.TEXT_HEADING);
        tierRow.add(lblCap);
        tierRow.add(badgeTierCard);

        lblDiskonMember.setFont(UiUtil.fontLabel(13f));
        lblDiskonMember.setForeground(UiUtil.PRIMARY_END);
        lblDiskonMember.setBorder(new EmptyBorder(4, 10, 0, 0));
        lblDiskonMember.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStatusUpgrade.setFont(UiUtil.fontBody(12.5f));
        lblStatusUpgrade.setForeground(UiUtil.TEXT_MUTED);
        lblStatusUpgrade.setBorder(new EmptyBorder(4, 10, 0, 0));
        lblStatusUpgrade.setAlignmentX(Component.LEFT_ALIGNMENT);

        statusLeft.add(tierRow);
        statusLeft.add(lblDiskonMember);
        statusLeft.add(lblStatusUpgrade);

        statusCard.add(statusLeft, BorderLayout.CENTER);

        // Kartu form pengajuan upgrade
        UiUtil.styleComboBox(cbPaketUpgrade);
        cbPaketUpgrade.setPreferredSize(new Dimension(225, 38));
        UiUtil.styleComboBox(cbMetodeUpgrade);
        cbMetodeUpgrade.setPreferredSize(new Dimension(122, 38));

        UiUtil.ModernButton btnRefresh = new UiUtil.ModernButton("Refresh", UiUtil.ModernButton.Variant.SECONDARY);
        btnAjukanUpgrade.addActionListener(e -> aksiAjukanUpgrade());
        btnRefresh.addActionListener(e -> refreshSemua());

        UiUtil.ShadowCardPanel formCard = new UiUtil.ShadowCardPanel(16, 6);
        formCard.setLayout(new BorderLayout(8, 0));
        formCard.setContentPadding(12, 14, 12, 14);

        JPanel formLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        formLeft.setOpaque(false);
        JLabel lblPaket = new JLabel("Paket:");
        lblPaket.setFont(UiUtil.fontLabel(13f));
        lblPaket.setForeground(UiUtil.TEXT_BODY);
        JLabel lblMet = new JLabel("Metode:");
        lblMet.setFont(UiUtil.fontLabel(13f));
        lblMet.setForeground(UiUtil.TEXT_BODY);

        formLeft.add(lblPaket);
        formLeft.add(cbPaketUpgrade);
        formLeft.add(Box.createHorizontalStrut(4));
        formLeft.add(lblMet);
        formLeft.add(cbMetodeUpgrade);
        formLeft.add(Box.createHorizontalStrut(4));
        formLeft.add(btnAjukanUpgrade);

        JPanel formRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        formRight.setOpaque(false);
        formRight.add(btnRefresh);

        formCard.add(formLeft, BorderLayout.WEST);
        formCard.add(formRight, BorderLayout.EAST);

        // Kartu tabel riwayat pengajuan upgrade
        JLabel lblRiwayatUp = new JLabel("Riwayat Pengajuan Upgrade");
        lblRiwayatUp.setFont(UiUtil.fontHeading(15f));
        lblRiwayatUp.setForeground(UiUtil.TEXT_HEADING);
        lblRiwayatUp.setBorder(new EmptyBorder(0, 2, 10, 0));

        JScrollPane spUpgrade = new JScrollPane(tUpgrade);
        UiUtil.styleScrollPane(spUpgrade);

        UiUtil.ShadowCardPanel tableCard = new UiUtil.ShadowCardPanel(16, 7);
        tableCard.setLayout(new BorderLayout());
        tableCard.setContentPadding(14, 16, 14, 16);
        tableCard.add(lblRiwayatUp, BorderLayout.NORTH);
        tableCard.add(spUpgrade, BorderLayout.CENTER);

        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        topStack.add(header);
        topStack.add(Box.createVerticalStrut(12));
        topStack.add(statusCard);
        topStack.add(Box.createVerticalStrut(10));
        topStack.add(formCard);

        root.add(topStack, BorderLayout.NORTH);
        root.add(tableCard, BorderLayout.CENTER);
        return root;
    }

    private void aksiAjukanUpgrade() {
        PaketUpgrade paket = (PaketUpgrade) cbPaketUpgrade.getSelectedItem();
        if (paket == null) {
            UiUtil.error(this, "Tidak ada paket upgrade yang tersedia untuk tier Anda saat ini.");
            return;
        }
        String metode = (String) cbMetodeUpgrade.getSelectedItem();
        String pesan = String.format("Ajukan upgrade member %s -> %s?%nBiaya  : %s%nMetode : %s%n%n%s",
                user.getMember(), paket.target, Fmt.rp(paket.biaya), metode,
                "E-Wallet".equals(metode)
                        ? "Pembayaran E-Wallet langsung lunas dan tier aktif segera."
                        : "Pembayaran " + metode + " menunggu konfirmasi admin sebelum tier berubah.");
        if (!UiUtil.konfirmasi(this, pesan)) return;
        try {
            UpgradeMember u = us.ajukanUpgrade(user, paket.target, Pembayaran.dari(metode), LocalDateTime.now());
            refreshSemua();
            UiUtil.info(this, u.getStatus() == StatusUpgrade.LUNAS
                    ? "Upgrade berhasil! Tier member Anda kini " + user.getMember() + " (" + user.getDiskon().getNama() + ")."
                    : "Pengajuan upgrade " + u.getId() + " dicatat. Menunggu konfirmasi admin.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            refreshSemua();
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void refreshUpgrade() {
        PaketUpgrade terpilih = (PaketUpgrade) cbPaketUpgrade.getSelectedItem();
        Member targetSebelumnya = terpilih == null ? null : terpilih.target;
        cbPaketUpgrade.removeAllItems();
        List<Member> tersedia = us.getTargetUpgradeTersedia(user.getMember());
        int indeksPilih = 0;
        for (int i = 0; i < tersedia.size(); i++) {
            Member target = tersedia.get(i);
            cbPaketUpgrade.addItem(new PaketUpgrade(target, us.hitungBiaya(user.getMember(), target)));
            if (target == targetSebelumnya) indeksPilih = i;
        }
        if (cbPaketUpgrade.getItemCount() > 0) cbPaketUpgrade.setSelectedIndex(indeksPilih);

        boolean sudahMaks = tersedia.isEmpty();
        boolean adaMenunggu = us.adaPengajuanMenunggu(user);
        boolean bolehAjukan = !sudahMaks && !adaMenunggu;
        cbPaketUpgrade.setEnabled(bolehAjukan);
        cbMetodeUpgrade.setEnabled(bolehAjukan);
        btnAjukanUpgrade.setEnabled(bolehAjukan);

        if (sudahMaks) {
            lblStatusUpgrade.setText("Anda sudah berada di tier tertinggi (GOLD - Diskon 10%).");
        } else if (adaMenunggu) {
            lblStatusUpgrade.setText("Anda memiliki pengajuan upgrade yang sedang menunggu konfirmasi admin.");
        } else {
            lblStatusUpgrade.setText("E-Wallet langsung lunas & aktif. Transfer/Tunai menunggu konfirmasi admin.");
        }

        List<UpgradeMember> data = new ArrayList<>(us.getPengajuanPelanggan(user));
        Collections.reverse(data);
        UiUtil.isiTabelUpgrade(tUpgrade, mUpgrade, data, false);
    }

    private void refreshRiwayat() {
        List<Booking> data = new ArrayList<>(bs.getBookingPelanggan(user));
        Collections.reverse(data);
        UiUtil.isiTabelBooking(tRiwayat, mRiwayat, data, false, LocalDateTime.now());
    }

    private void refreshSemua() {
        us.segarkanPelanggan(user);
        perbaruiInfoMember();
        bs.perbaruiStatus(LocalDateTime.now());
        refreshJadwal();
        refreshRiwayat();
        refreshUpgrade();
    }

    private static class PaketUpgrade {
        final Member target;
        final double biaya;

        PaketUpgrade(Member target, double biaya) {
            this.target = target;
            this.biaya = biaya;
        }

        @Override public String toString() {
            return target + " (Diskon " + target.getPersen() + "%) - " + Fmt.rp(biaya);
        }
    }
}



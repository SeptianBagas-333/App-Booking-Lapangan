package view;

import exception.LoginGagalException;
import model.*;
import service.AuthService;
import service.BookingService;
import service.UpgradeMemberService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;

/** Layar autentikasi modern bergaya kartu melayang responsif (soft light + teal gradient). */
public class LoginFrame extends JFrame {
    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_REGISTER = "REGISTER";

    private static final int CARD_MIN_W = 860;
    private static final int CARD_MAX_W = 1120;
    private static final int CARD_MIN_H = 540;
    private static final int CARD_MAX_H = 680;
    private static final int COLLAPSE_BRAND_WIDTH = 820;

    private final AuthService auth;
    private final BookingService bs;
    private final UpgradeMemberService us;

    // Input mode Masuk
    private final UiUtil.ModernTextField tfUser =
            new UiUtil.ModernTextField("Masukkan username", UiUtil.InputIcon.USER);
    private final UiUtil.ModernPasswordField pfPass =
            new UiUtil.ModernPasswordField("Masukkan password");
    private final UiUtil.ModernButton btnMasuk =
            new UiUtil.ModernButton("Masuk", UiUtil.ModernButton.Variant.PRIMARY);

    // Input mode Daftar Pelanggan Baru
    private final UiUtil.ModernTextField tfRegNama =
            new UiUtil.ModernTextField("Masukkan nama lengkap", UiUtil.InputIcon.BADGE);
    private final UiUtil.ModernTextField tfRegUser =
            new UiUtil.ModernTextField("Minimal 3 karakter", UiUtil.InputIcon.USER);
    private final UiUtil.ModernPasswordField pfRegPass =
            new UiUtil.ModernPasswordField("Minimal 4 karakter");
    private final UiUtil.ModernButton btnSubmitDaftar =
            new UiUtil.ModernButton("Buat Akun Pelanggan", UiUtil.ModernButton.Variant.PRIMARY);

    // Navigasi kartu internal
    private final CardLayout formCards = new CardLayout();
    private final JPanel formCardHost = new JPanel(formCards);

    private JPanel brandPane;
    private JPanel rightPane;

    public LoginFrame(AuthService auth, BookingService bs, UpgradeMemberService us) {
        super("Sistem Booking Lapangan Olahraga");
        this.auth = auth;
        this.bs = bs;
        this.us = us;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        brandPane = buildBrandPane();
        rightPane = buildRightPane();

        // Kartu utama melayang dengan pembagian proporsional 48% kiri : 52% kanan
        UiUtil.ShadowCardPanel floatingCard = new UiUtil.ShadowCardPanel(22, 14) {
            @Override
            public void doLayout() {
                Insets in = getInsets();
                int innerW = Math.max(0, getWidth() - in.left - in.right);
                int innerH = Math.max(0, getHeight() - in.top - in.bottom);
                if (brandPane.isVisible()) {
                    int leftW = (int) Math.round(innerW * 0.48);
                    int rightW = innerW - leftW;
                    brandPane.setBounds(in.left, in.top, leftW, innerH);
                    rightPane.setBounds(in.left + leftW, in.top, rightW, innerH);
                    brandPane.doLayout();
                } else {
                    rightPane.setBounds(in.left, in.top, innerW, innerH);
                }
                rightPane.doLayout();
            }
        };
        floatingCard.setLayout(null);
        floatingCard.add(brandPane);
        floatingCard.add(rightPane);

        // Canvas latar gradasi terang dengan layout responsif (68-72% lebar, 70-75% tinggi, clamp 860x540..1120x680)
        UiUtil.GradientCanvasPanel canvas =
                new UiUtil.GradientCanvasPanel(UiUtil.BG_CANVAS_START, UiUtil.BG_LOGIN_END, true) {
                    @Override
                    public void doLayout() {
                        int cw = getWidth();
                        int ch = getHeight();
                        if (cw <= 0 || ch <= 0) return;

                        boolean showBrand = cw >= COLLAPSE_BRAND_WIDTH;
                        if (brandPane.isVisible() != showBrand) {
                            brandPane.setVisible(showBrand);
                        }

                        int cardW;
                        int cardH;
                        if (showBrand) {
                            int targetW = (int) Math.round(cw * 0.70);
                            int targetH = (int) Math.round(ch * 0.74);
                            cardW = Math.max(CARD_MIN_W, Math.min(CARD_MAX_W, targetW));
                            cardH = Math.max(CARD_MIN_H, Math.min(CARD_MAX_H, targetH));
                            cardW = Math.min(cardW, Math.max(520, cw - 36));
                            cardH = Math.min(cardH, Math.max(500, ch - 36));
                        } else {
                            cardW = Math.min(500, Math.max(380, cw - 32));
                            cardH = Math.max(540, Math.min(640, ch - 32));
                        }

                        int x = (cw - cardW) / 2;
                        int y = (ch - cardH) / 2;
                        floatingCard.setBounds(x, y, cardW, cardH);
                        floatingCard.doLayout();
                    }
                };
        canvas.setLayout(null);
        canvas.add(floatingCard);

        setContentPane(canvas);
        getRootPane().setDefaultButton(btnMasuk);

        setSize(1080, 700);
        setMinimumSize(new Dimension(520, 600));
        setLocationRelativeTo(null);
    }

    /** Panel info & branding ringan di sisi kiri kartu melayang (~48% lebar kartu). */
    private JPanel buildBrandPane() {
        JPanel pane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtil.enableAntiAliasing(g2);
                int w = getWidth();
                int h = getHeight();

                // Sudut kiri membulat mengikuti kartu utama
                Shape clip = new RoundRectangle2D.Double(0, 0, w + 24, h, 22, 22);
                g2.setClip(clip);
                g2.setPaint(new GradientPaint(0, 0, new Color(0xF0, 0xFD, 0xFA), w, h, new Color(0xCC, 0xFB, 0xF1)));
                g2.fillRect(0, 0, w, h);

                // Aksen garis lapangan olahraga tipis proporsional
                double scale = Math.min(w, h);
                g2.setColor(new Color(0x0D, 0x94, 0x88, 22));
                g2.setStroke(new BasicStroke(1.6f));
                double r1 = scale * 0.44;
                g2.draw(new Ellipse2D.Double(w - r1 * 0.62, h - r1 * 0.68, r1, r1));
                double r2 = scale * 0.40;
                g2.draw(new Ellipse2D.Double(-r2 * 0.34, -r2 * 0.34, r2, r2));
                g2.draw(new RoundRectangle2D.Double(-44, h - scale * 0.34, scale * 0.36, scale * 0.48, 20, 20));

                // Garis pemisah halus kanan
                g2.setClip(null);
                g2.setColor(UiUtil.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawLine(w - 1, 0, w - 1, h);

                g2.dispose();
            }
        };
        pane.setOpaque(false);
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.setBorder(new EmptyBorder(40, 42, 38, 38));

        // Badge aplikasi di atas (rata kiri)
        JPanel brandHeader = new JPanel();
        brandHeader.setOpaque(false);
        brandHeader.setLayout(new BoxLayout(brandHeader, BoxLayout.X_AXIS));
        brandHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandHeader.add(new CourtEmblem(48));
        brandHeader.add(Box.createHorizontalStrut(14));

        JPanel brandTitles = new JPanel();
        brandTitles.setOpaque(false);
        brandTitles.setLayout(new BoxLayout(brandTitles, BoxLayout.Y_AXIS));
        JLabel appName = new JLabel("ArenaBook");
        appName.setFont(UiUtil.fontHeading(21f));
        appName.setForeground(UiUtil.TEXT_HEADING);
        JLabel appTag = new JLabel("Booking Lapangan Olahraga");
        appTag.setFont(UiUtil.fontBody(13f));
        appTag.setForeground(UiUtil.PRIMARY_END);
        brandTitles.add(appName);
        brandTitles.add(Box.createVerticalStrut(2));
        brandTitles.add(appTag);
        brandHeader.add(brandTitles);
        brandHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        // Headline utama panel kiri (wrapping fleksibel mengikuti lebar panel)
        JTextArea headline = createWrappingText(
                "Pesan jadwal main\nlebih cepat & teratur.",
                UiUtil.fontHeading(26f),
                UiUtil.TEXT_HEADING
        );

        JTextArea desc = createWrappingText(
                "Cek ketersediaan slot secara real-time, nikmati diskon member, dan terima struk booking secara instan.",
                UiUtil.fontBody(13.5f),
                UiUtil.TEXT_BODY
        );

        // Chip 5 jenis lapangan (2 baris rapat dengan gap vertikal 8px)
        JPanel chips = new JPanel();
        chips.setOpaque(false);
        chips.setLayout(new BoxLayout(chips, BoxLayout.Y_AXIS));
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chipRow1 = createChipRow("Futsal", "Minisoccer", "Basket");
        JPanel chipRow2 = createChipRow("Badminton", "Padel");

        chips.add(chipRow1);
        chips.add(Box.createVerticalStrut(8));
        chips.add(chipRow2);
        chips.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                chipRow1.getPreferredSize().height + 8 + chipRow2.getPreferredSize().height));

        // Daftar keunggulan di bagian bawah (sejajar rapi)
        JPanel highlights = new JPanel();
        highlights.setOpaque(false);
        highlights.setLayout(new BoxLayout(highlights, BoxLayout.Y_AXIS));
        highlights.setAlignmentX(Component.LEFT_ALIGNMENT);
        highlights.add(createHighlightRow("Jadwal lapangan transparan per jam"));
        highlights.add(Box.createVerticalStrut(10));
        highlights.add(createHighlightRow("Benefit member Silver 5% & Gold 10%"));
        highlights.add(Box.createVerticalStrut(10));
        highlights.add(createHighlightRow("Pembayaran E-Wallet, Transfer, & Tunai"));
        highlights.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));

        pane.add(brandHeader);
        pane.add(Box.createVerticalStrut(28));
        pane.add(headline);
        pane.add(Box.createVerticalStrut(12));
        pane.add(desc);
        pane.add(Box.createVerticalStrut(22));
        pane.add(chips);
        pane.add(Box.createVerticalGlue());
        pane.add(Box.createVerticalStrut(16));
        pane.add(highlights);

        return pane;
    }

    /** TextArea non-editable transparan yang menghitung tinggi multi-baris akurat sesuai lebar panel. */
    private JTextArea createWrappingText(String text, Font font, Color color) {
        JTextArea ta = new JTextArea(text) {
            private int computeWrappedHeight(int availW) {
                FontMetrics fm = getFontMetrics(getFont());
                int lineH = fm.getHeight();
                if (availW <= 40) availW = 320;
                int lines = 0;
                for (String para : getText().split("\n", -1)) {
                    if (para.isEmpty()) { lines++; continue; }
                    String[] words = para.split(" ");
                    int curW = 0;
                    int paraLines = 1;
                    for (String word : words) {
                        int ww = fm.stringWidth(word);
                        int sp = curW == 0 ? 0 : fm.stringWidth(" ");
                        if (curW > 0 && curW + sp + ww > availW) {
                            paraLines++;
                            curW = ww;
                        } else {
                            curW += sp + ww;
                        }
                    }
                    lines += paraLines;
                }
                return Math.max(lineH, lines * lineH + 4);
            }

            @Override
            public Dimension getPreferredSize() {
                Container p = getParent();
                int availW = 320;
                if (p != null && p.getWidth() > 0) {
                    Insets pi = p.getInsets();
                    availW = Math.max(120, p.getWidth() - pi.left - pi.right);
                }
                return new Dimension(availW, computeWrappedHeight(availW));
            }

            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        ta.setFont(font);
        ta.setForeground(color);
        ta.setOpaque(false);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(null);
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return ta;
    }

    private JPanel createChipRow(String... labels) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) row.add(Box.createHorizontalStrut(8));
            row.add(createSportChip(labels[i]));
        }
        row.add(Box.createHorizontalGlue());
        int rowH = row.getPreferredSize().height;
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowH));
        return row;
    }

    private JLabel createSportChip(String text) {
        JLabel chip = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtil.enableAntiAliasing(g2);
                g2.setColor(new Color(255, 255, 255, 225));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.setColor(new Color(0x99, 0xF6, 0xE4));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setFont(UiUtil.fontLabel(12f));
        chip.setForeground(UiUtil.PRIMARY_PRESSED);
        chip.setBorder(new EmptyBorder(5, 13, 5, 13));
        return chip;
    }

    private JPanel createHighlightRow(String text) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JComponent check = new JComponent() {
            @Override public Dimension getPreferredSize() { return new Dimension(18, 18); }
            @Override public Dimension getMinimumSize() { return new Dimension(18, 18); }
            @Override public Dimension getMaximumSize() { return new Dimension(18, 18); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UiUtil.enableAntiAliasing(g2);
                g2.setColor(UiUtil.PRIMARY_START);
                g2.fill(new Ellipse2D.Double(0, 0, 18, 18));
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(5, 9, 8, 12);
                g2.drawLine(8, 12, 13, 6);
                g2.dispose();
            }
        };
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiUtil.fontBody(13.5f));
        lbl.setForeground(UiUtil.TEXT_BODY);
        row.add(check);
        row.add(Box.createHorizontalStrut(10));
        row.add(lbl);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    /** Panel kanan kartu putih (~52% lebar kartu) yang memuat mode Masuk dan mode Daftar. */
    private JPanel buildRightPane() {
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(36, 44, 34, 44));

        formCardHost.setOpaque(false);
        formCardHost.add(buildLoginCard(), CARD_LOGIN);
        formCardHost.add(buildRegisterCard(), CARD_REGISTER);

        right.add(formCardHost, BorderLayout.CENTER);
        return right;
    }

    private JPanel buildLoginCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Emblem vektor lapangan olahraga
        JPanel topEmblem = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        topEmblem.setOpaque(false);
        topEmblem.setAlignmentX(Component.CENTER_ALIGNMENT);
        topEmblem.add(new CourtEmblem(50));
        topEmblem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel title = new JLabel("Selamat Datang Kembali", SwingConstants.CENTER);
        title.setFont(UiUtil.fontHeading(26f));
        title.setForeground(UiUtil.TEXT_HEADING);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Masuk untuk mulai memesan lapangan.", SwingConstants.CENTER);
        subtitle.setFont(UiUtil.fontBody(13.5f));
        subtitle.setForeground(UiUtil.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form input lebar responsif dengan batas atas proporsional
        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setAlignmentX(Component.CENTER_ALIGNMENT);
        fields.setMaximumSize(new Dimension(420, 260));

        JLabel lblUser = createFieldLabel("Username");
        tfUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tfUser.setPreferredSize(new Dimension(340, 46));
        tfUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfUser.addActionListener(e -> pfPass.requestFocusInWindow());

        JLabel lblPass = createFieldLabel("Password");
        pfPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        pfPass.setPreferredSize(new Dimension(340, 46));
        pfPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pfPass.addActionListener(e -> login());

        btnMasuk.setFont(UiUtil.fontLabel(14.5f));
        btnMasuk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnMasuk.setPreferredSize(new Dimension(340, 46));
        btnMasuk.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnMasuk.addActionListener(e -> login());

        fields.add(lblUser);
        fields.add(Box.createVerticalStrut(7));
        fields.add(tfUser);
        fields.add(Box.createVerticalStrut(15));
        fields.add(lblPass);
        fields.add(Box.createVerticalStrut(7));
        fields.add(pfPass);
        fields.add(Box.createVerticalStrut(22));
        fields.add(btnMasuk);

        // Aksi pindah ke Daftar Pelanggan Baru (selalu 1 baris rapi)
        JPanel switchRow = new JPanel();
        switchRow.setOpaque(false);
        switchRow.setLayout(new BoxLayout(switchRow, BoxLayout.X_AXIS));
        switchRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel belum = new JLabel("Belum punya akun?");
        belum.setFont(UiUtil.fontBody(13.5f));
        belum.setForeground(UiUtil.TEXT_MUTED);

        UiUtil.ModernButton btnKeDaftar =
                new UiUtil.ModernButton("Daftar Pelanggan Baru", UiUtil.ModernButton.Variant.GHOST, 8);
        btnKeDaftar.setFont(UiUtil.fontLabel(13.5f));
        btnKeDaftar.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnKeDaftar.addActionListener(e -> switchToRegister());

        switchRow.add(Box.createHorizontalGlue());
        switchRow.add(belum);
        switchRow.add(Box.createHorizontalStrut(4));
        switchRow.add(btnKeDaftar);
        switchRow.add(Box.createHorizontalGlue());
        switchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        panel.add(Box.createVerticalGlue());
        panel.add(topEmblem);
        panel.add(Box.createVerticalStrut(14));
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(24));
        panel.add(fields);
        panel.add(Box.createVerticalStrut(16));
        panel.add(switchRow);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildRegisterCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Daftar Pelanggan Baru", SwingConstants.CENTER);
        title.setFont(UiUtil.fontHeading(26f));
        title.setForeground(UiUtil.TEXT_HEADING);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Buat akun gratis untuk memesan lapangan.", SwingConstants.CENTER);
        subtitle.setFont(UiUtil.fontBody(13.5f));
        subtitle.setForeground(UiUtil.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setAlignmentX(Component.CENTER_ALIGNMENT);
        fields.setMaximumSize(new Dimension(420, 355));

        tfRegNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tfRegNama.setPreferredSize(new Dimension(340, 46));
        tfRegNama.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfRegNama.addActionListener(e -> tfRegUser.requestFocusInWindow());

        tfRegUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        tfRegUser.setPreferredSize(new Dimension(340, 46));
        tfRegUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        tfRegUser.addActionListener(e -> pfRegPass.requestFocusInWindow());

        pfRegPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        pfRegPass.setPreferredSize(new Dimension(340, 46));
        pfRegPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        pfRegPass.addActionListener(e -> submitRegistrasi());

        btnSubmitDaftar.setFont(UiUtil.fontLabel(14.5f));
        btnSubmitDaftar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnSubmitDaftar.setPreferredSize(new Dimension(340, 46));
        btnSubmitDaftar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubmitDaftar.addActionListener(e -> submitRegistrasi());

        UiUtil.ModernButton btnKembali =
                new UiUtil.ModernButton("Kembali ke Masuk", UiUtil.ModernButton.Variant.SECONDARY);
        btnKembali.setFont(UiUtil.fontLabel(13.5f));
        btnKembali.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnKembali.setPreferredSize(new Dimension(340, 42));
        btnKembali.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnKembali.addActionListener(e -> switchToLogin());

        fields.add(createFieldLabel("Nama Lengkap"));
        fields.add(Box.createVerticalStrut(6));
        fields.add(tfRegNama);
        fields.add(Box.createVerticalStrut(12));
        fields.add(createFieldLabel("Username"));
        fields.add(Box.createVerticalStrut(6));
        fields.add(tfRegUser);
        fields.add(Box.createVerticalStrut(12));
        fields.add(createFieldLabel("Password"));
        fields.add(Box.createVerticalStrut(6));
        fields.add(pfRegPass);
        fields.add(Box.createVerticalStrut(18));
        fields.add(btnSubmitDaftar);
        fields.add(Box.createVerticalStrut(10));
        fields.add(btnKembali);

        panel.add(Box.createVerticalGlue());
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(22));
        panel.add(fields);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiUtil.fontLabel(13f));
        lbl.setForeground(UiUtil.TEXT_BODY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void switchToRegister() {
        tfRegNama.setText("");
        tfRegUser.setText("");
        pfRegPass.setText("");
        pfRegPass.resetVisibility();
        formCards.show(formCardHost, CARD_REGISTER);
        getRootPane().setDefaultButton(btnSubmitDaftar);
        SwingUtilities.invokeLater(tfRegNama::requestFocusInWindow);
    }

    private void switchToLogin() {
        pfPass.resetVisibility();
        formCards.show(formCardHost, CARD_LOGIN);
        getRootPane().setDefaultButton(btnMasuk);
        SwingUtilities.invokeLater(tfUser::requestFocusInWindow);
    }

    private void login() {
        try {
            User u = auth.login(tfUser.getText(), new String(pfPass.getPassword()));
            bs.perbaruiStatus(LocalDateTime.now());
            JFrame next = (u instanceof Admin)
                    ? new AdminFrame((Admin) u, bs, us, auth)
                    : new PelangganFrame((Pelanggan) u, bs, us, auth);
            UiUtil.tampilkanMaksimal(next);
            dispose();
        } catch (LoginGagalException ex) {
            UiUtil.error(this, ex.getMessage());
            pfPass.setText("");
            pfPass.requestFocusInWindow();
        }
    }

    private void submitRegistrasi() {
        try {
            String usernameBaru = tfRegUser.getText().trim();
            auth.registrasi(usernameBaru, new String(pfRegPass.getPassword()), tfRegNama.getText());
            UiUtil.info(this, "Registrasi berhasil. Silakan masuk dengan akun baru Anda.");
            tfUser.setText(usernameBaru);
            pfPass.setText("");
            switchToLogin();
            SwingUtilities.invokeLater(pfPass::requestFocusInWindow);
        } catch (LoginGagalException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    /** Emblem vektor lapangan olahraga (Graphics2D murni tanpa file gambar eksternal). */
    private static final class CourtEmblem extends JComponent {
        private final int size;

        CourtEmblem(int size) {
            this.size = size;
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            UiUtil.enableAntiAliasing(g2);

            int arc = Math.round(size * 0.30f);
            g2.setPaint(new GradientPaint(0, 0, UiUtil.PRIMARY_START, size, size, UiUtil.PRIMARY_END));
            g2.fill(new RoundRectangle2D.Double(0, 0, size, size, arc, arc));

            // Gambar garis lapangan olahraga minimalis di dalam emblem
            double pad = size * 0.20;
            double cw = size - pad * 2;
            double ch = size * 0.44;
            double cy = (size - ch) / 2.0;

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size * 0.042f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(pad, cy, cw, ch, 4, 4));

            // Garis tengah & lingkaran tengah lapangan
            double midX = size / 2.0;
            double midY = size / 2.0;
            g2.drawLine((int) Math.round(midX), (int) Math.round(cy), (int) Math.round(midX), (int) Math.round(cy + ch));
            double cr = ch * 0.28;
            g2.draw(new Ellipse2D.Double(midX - cr, midY - cr, cr * 2, cr * 2));

            // Area penalti kiri & kanan
            double boxW = cw * 0.16;
            double boxH = ch * 0.50;
            g2.draw(new Arc2D.Double(pad - boxW / 2, midY - boxH / 2, boxW, boxH, -90, 180, Arc2D.OPEN));
            g2.draw(new Arc2D.Double(pad + cw - boxW / 2, midY - boxH / 2, boxW, boxH, 90, 180, Arc2D.OPEN));

            g2.dispose();
        }
    }
}

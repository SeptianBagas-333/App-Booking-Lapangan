package view;

import model.*;
import util.Fmt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fondasi komponen UI modern (soft light + teal gradient) dan helper tampilan bersama.
 * Mempertahankan seluruh konstanta & method lama demi kompatibilitas seluruh proyek.
 */
public final class UiUtil {

    // ===== Palet Warna Soft Light Modern =====
    static final Color BG_CANVAS_START = new Color(0xF8, 0xFA, 0xFC);      // #F8FAFC
    static final Color BG_CANVAS_END = new Color(0xEF, 0xF8, 0xF7);        // #EFF8F7
    static final Color BG_LOGIN_END = new Color(0xE6, 0xFF, 0xFA);         // #E6FFFA

    static final Color SURFACE_CARD = Color.WHITE;                         // #FFFFFF
    static final Color SURFACE_SIDEBAR = new Color(0xF8, 0xFA, 0xFC);      // #F8FAFC
    static final Color SURFACE_SUBTLE = new Color(0xF1, 0xF5, 0xF9);       // #F1F5F9
    static final Color BORDER_SUBTLE = new Color(0xE2, 0xE8, 0xF0);        // #E2E8F0
    static final Color BORDER_INPUT = new Color(0xCB, 0xD5, 0xE1);         // #CBD5E1
    static final Color BORDER_FOCUS = new Color(0x14, 0xB8, 0xA6);         // #14B8A6

    static final Color PRIMARY_START = new Color(0x14, 0xB8, 0xA6);        // #14B8A6
    static final Color PRIMARY_END = new Color(0x0D, 0x94, 0x88);          // #0D9488
    static final Color PRIMARY_HOVER_START = new Color(0x0D, 0x94, 0x88);  // #0D9488
    static final Color PRIMARY_HOVER_END = new Color(0x0F, 0x76, 0x6E);    // #0F766E
    static final Color PRIMARY_PRESSED = new Color(0x11, 0x5E, 0x59);      // #115E59
    static final Color PRIMARY_SOFT_BG = new Color(0xE6, 0xFF, 0xFA);      // #E6FFFA
    static final Color PRIMARY_SOFT_ALT = new Color(0xCC, 0xFB, 0xF1);     // #CCFBF1

    static final Color DANGER_START = new Color(0xF4, 0x3F, 0x5E);         // #F43F5E
    static final Color DANGER_END = new Color(0xE1, 0x1D, 0x48);           // #E11D48
    static final Color DANGER_PRESSED = new Color(0xBE, 0x12, 0x3C);       // #BE123C
    static final Color DANGER_SOFT_BG = new Color(0xFF, 0xF1, 0xF2);       // #FFF1F2
    static final Color DANGER_SOFT_BORDER = new Color(0xFE, 0xCD, 0xD3);   // #FECDD3

    static final Color TEXT_HEADING = new Color(0x0F, 0x17, 0x2A);         // #0F172A
    static final Color TEXT_BODY = new Color(0x33, 0x41, 0x55);            // #334155
    static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);           // #64748B

    // ===== Warna Semantik Slot & Pill Badge Pastel =====
    static final Color BADGE_GREEN_BG = new Color(0xDC, 0xFC, 0xE7);       // #DCFCE7
    static final Color BADGE_GREEN_ACCENT = new Color(0x10, 0xB9, 0x81);   // #10B981
    static final Color BADGE_GREEN_TEXT = new Color(0x06, 0x5F, 0x46);     // #065F46

    static final Color BADGE_RED_BG = new Color(0xFF, 0xE4, 0xE6);         // #FFE4E6
    static final Color BADGE_RED_ACCENT = new Color(0xF4, 0x3F, 0x5E);     // #F43F5E
    static final Color BADGE_RED_TEXT = new Color(0x9F, 0x12, 0x39);       // #9F1239

    static final Color BADGE_BLUE_BG = new Color(0xE0, 0xF2, 0xFE);        // #E0F2FE
    static final Color BADGE_BLUE_ACCENT = new Color(0x0E, 0xA5, 0xE9);    // #0EA5E9
    static final Color BADGE_BLUE_TEXT = new Color(0x07, 0x59, 0x85);      // #075985

    static final Color BADGE_AMBER_BG = new Color(0xFE, 0xF3, 0xC7);       // #FEF3C7
    static final Color BADGE_AMBER_ACCENT = new Color(0xF5, 0x9E, 0x0B);   // #F59E0B
    static final Color BADGE_AMBER_TEXT = new Color(0x92, 0x40, 0x0E);     // #92400E

    static final Color BADGE_GRAY_BG = new Color(0xF1, 0xF5, 0xF9);        // #F1F5F9
    static final Color BADGE_GRAY_ACCENT = new Color(0xCB, 0xD5, 0xE1);    // #CBD5E1
    static final Color BADGE_GRAY_TEXT = new Color(0x94, 0xA3, 0xB8);      // #94A3B8

    static final Color TABLE_HOVER_BG = new Color(0xF8, 0xFA, 0xFC);       // #F8FAFC
    static final Color TABLE_SELECT_BG = new Color(0xE6, 0xFF, 0xFA);      // #E6FFFA

    // ===== Konstanta Lama (Kompatibilitas) =====
    static final Color HIJAU = BADGE_GREEN_BG;
    static final Color MERAH = BADGE_RED_BG;
    static final Color BIRU = BADGE_BLUE_BG;
    static final Color ABU = BADGE_GRAY_BG;

    private static final String PROP_HOVER_ROW = "ui.hoverRow";
    private static final String PROP_COMBO_HOVER = "ui.comboHover";
    static final Color SCROLL_THUMB_HOVER = new Color(0x94, 0xA3, 0xB8);   // #94A3B8

    // ===== Sistem Tipografi Lintas Platform (Linux & Windows) =====
    private static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        String[] candidates = {"Inter", "Segoe UI", "Ubuntu", "Noto Sans", "Liberation Sans", "DejaVu Sans"};
        try {
            Set<String> available = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()
            ));
            for (String candidate : candidates) {
                if (available.contains(candidate)) return candidate;
            }
        } catch (Exception ignored) {
            // fallback ke SansSerif
        }
        return Font.SANS_SERIF;
    }

    static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, Math.round(size));
    }

    static Font fontHeading(float size) { return font(Font.BOLD, size); }
    static Font fontBody(float size) { return font(Font.PLAIN, size); }
    static Font fontLabel(float size) { return font(Font.BOLD, size); }

    /** Mengaktifkan rendering anti-aliasing berkualitas tinggi pada Graphics2D. */
    static void enableAntiAliasing(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static boolean temaTerangInitialized = false;

    static {
        initTemaTerang();
    }

    /**
     * Menginisialisasi tema soft light lintas platform (CrossPlatformLookAndFeel + UIManager defaults)
     * agar komponen bawaan Swing tidak terpengaruh tema gelap GTK di Linux Ubuntu.
     */
    public static synchronized void initTemaTerang() {
        if (temaTerangInitialized) return;
        temaTerangInitialized = true;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Font defaultBody = fontBody(13f);
        Font defaultBold = fontLabel(13f);

        UIManager.put("control", SURFACE_CARD);
        UIManager.put("window", SURFACE_CARD);
        UIManager.put("Panel.background", SURFACE_CARD);
        UIManager.put("Panel.foreground", TEXT_HEADING);
        UIManager.put("OptionPane.background", SURFACE_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_HEADING);
        UIManager.put("Label.foreground", TEXT_HEADING);
        UIManager.put("Label.font", defaultBody);

        UIManager.put("TextField.background", SURFACE_CARD);
        UIManager.put("TextField.foreground", TEXT_HEADING);
        UIManager.put("TextField.caretForeground", PRIMARY_END);
        UIManager.put("TextField.selectionBackground", PRIMARY_SOFT_ALT);
        UIManager.put("TextField.selectionForeground", PRIMARY_HOVER_END);
        UIManager.put("TextField.inactiveForeground", TEXT_MUTED);
        UIManager.put("TextField.font", defaultBody);

        UIManager.put("FormattedTextField.background", SURFACE_CARD);
        UIManager.put("FormattedTextField.foreground", TEXT_HEADING);
        UIManager.put("FormattedTextField.caretForeground", PRIMARY_END);
        UIManager.put("FormattedTextField.selectionBackground", PRIMARY_SOFT_ALT);
        UIManager.put("FormattedTextField.selectionForeground", PRIMARY_HOVER_END);
        UIManager.put("FormattedTextField.font", defaultBody);

        UIManager.put("PasswordField.background", SURFACE_CARD);
        UIManager.put("PasswordField.foreground", TEXT_HEADING);
        UIManager.put("PasswordField.caretForeground", PRIMARY_END);
        UIManager.put("PasswordField.selectionBackground", PRIMARY_SOFT_ALT);
        UIManager.put("PasswordField.selectionForeground", PRIMARY_HOVER_END);

        UIManager.put("TextArea.background", SURFACE_CARD);
        UIManager.put("TextArea.foreground", TEXT_HEADING);
        UIManager.put("TextArea.selectionBackground", PRIMARY_SOFT_ALT);
        UIManager.put("TextArea.selectionForeground", PRIMARY_HOVER_END);
        UIManager.put("TextArea.font", defaultBody);

        UIManager.put("ComboBox.background", SURFACE_CARD);
        UIManager.put("ComboBox.foreground", TEXT_HEADING);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_SOFT_BG);
        UIManager.put("ComboBox.selectionForeground", PRIMARY_HOVER_END);
        UIManager.put("ComboBox.buttonBackground", SURFACE_CARD);
        UIManager.put("ComboBox.buttonShadow", BORDER_SUBTLE);
        UIManager.put("ComboBox.buttonDarkShadow", BORDER_INPUT);
        UIManager.put("ComboBox.buttonHighlight", SURFACE_CARD);
        UIManager.put("ComboBox.disabledBackground", SURFACE_SUBTLE);
        UIManager.put("ComboBox.disabledForeground", BADGE_GRAY_TEXT);
        UIManager.put("ComboBox.font", defaultBody);

        UIManager.put("Spinner.background", SURFACE_CARD);
        UIManager.put("Spinner.foreground", TEXT_HEADING);
        UIManager.put("Spinner.font", defaultBold);

        UIManager.put("List.background", SURFACE_CARD);
        UIManager.put("List.foreground", TEXT_HEADING);
        UIManager.put("List.selectionBackground", PRIMARY_SOFT_BG);
        UIManager.put("List.selectionForeground", PRIMARY_HOVER_END);
        UIManager.put("List.font", defaultBody);

        UIManager.put("ScrollPane.background", SURFACE_CARD);
        UIManager.put("Viewport.background", SURFACE_CARD);
        UIManager.put("ScrollBar.background", SURFACE_CARD);
        UIManager.put("ScrollBar.track", SURFACE_CARD);
        UIManager.put("ScrollBar.thumb", BORDER_INPUT);
        UIManager.put("ScrollBar.thumbDarkShadow", BORDER_INPUT);
        UIManager.put("ScrollBar.thumbHighlight", BORDER_INPUT);
        UIManager.put("ScrollBar.thumbShadow", BORDER_INPUT);
        UIManager.put("ScrollBar.width", 10);

        UIManager.put("Table.background", SURFACE_CARD);
        UIManager.put("Table.foreground", TEXT_BODY);
        UIManager.put("Table.selectionBackground", TABLE_SELECT_BG);
        UIManager.put("Table.selectionForeground", TEXT_HEADING);
        UIManager.put("Table.gridColor", SURFACE_SUBTLE);
        UIManager.put("TableHeader.background", SURFACE_SIDEBAR);
        UIManager.put("TableHeader.foreground", TEXT_MUTED);

        UIManager.put("PopupMenu.background", SURFACE_CARD);
        UIManager.put("PopupMenu.foreground", TEXT_HEADING);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER_SUBTLE, 1));

        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("ToggleButton.focus", new Color(0, 0, 0, 0));
        UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));

        UIManager.put("ToolTip.background", SURFACE_CARD);
        UIManager.put("ToolTip.foreground", TEXT_HEADING);
        UIManager.put("ToolTip.font", fontBody(12f));
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_INPUT, 1),
                new EmptyBorder(5, 9, 5, 9)
        ));
    }

    private UiUtil() {}

    /**
     * Menampilkan jendela utama dalam keadaan maximized (layar penuh) secara konsisten
     * di GNOME/Ubuntu maupun Windows, sambil mempertahankan ukuran default saat di-restore.
     */
    public static void tampilkanMaksimal(JFrame frame) {
        if (frame == null) return;
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            if (frame.isDisplayable()) {
                frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            }
        });
    }

    // =========================================================================
    //  HELPER TABEL & DIALOG (Diperbarui dengan Gaya Soft Modern)
    // =========================================================================

    static DefaultTableModel modelTabel() {
        return new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable tabel(DefaultTableModel m) {
        JTable t = new JTable(m);
        styleModernTable(t);
        return t;
    }

    /** Menerapkan gaya modern ke JTable: tinggi baris 38px, hover halus, header bersih. */
    static void styleModernTable(JTable t) {
        t.setRowHeight(38);
        t.setFont(fontBody(13f));
        t.setForeground(TEXT_BODY);
        t.setBackground(SURFACE_CARD);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(SURFACE_SUBTLE);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(TABLE_SELECT_BG);
        t.setSelectionForeground(TEXT_HEADING);
        t.putClientProperty(PROP_HOVER_ROW, -1);

        JTableHeader header = t.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new ModernHeaderRenderer());

        t.setDefaultRenderer(Object.class, new ModernCellRenderer(SwingConstants.LEFT));

        t.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = t.rowAtPoint(e.getPoint());
                Integer prev = (Integer) t.getClientProperty(PROP_HOVER_ROW);
                if (prev == null || prev != row) {
                    t.putClientProperty(PROP_HOVER_ROW, row);
                    t.repaint();
                }
            }
        });
        t.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                t.putClientProperty(PROP_HOVER_ROW, -1);
                t.repaint();
            }
        });
    }

    static int getHoverRow(JTable t) {
        Object val = t.getClientProperty(PROP_HOVER_ROW);
        return val instanceof Integer ? (Integer) val : -1;
    }

    static void error(Component p, String pesan) {
        showMessageDialog(p, "Perhatian", pesan, BADGE_AMBER_BG, BADGE_AMBER_ACCENT, BADGE_AMBER_TEXT, "!");
    }

    static void info(Component p, String pesan) {
        showMessageDialog(p, "Informasi", pesan, PRIMARY_SOFT_BG, PRIMARY_START, PRIMARY_END, "i");
    }

    static boolean konfirmasi(Component p, String pesan) {
        return showConfirmDialog(p, "Konfirmasi", pesan, "Ya, Lanjutkan", ModernButton.Variant.PRIMARY);
    }

    static boolean konfirmasiDanger(Component p, String judul, String pesan, String labelOk) {
        return showConfirmDialog(p, judul, pesan, labelOk, ModernButton.Variant.DANGER);
    }

    private static Window resolveOwner(Component p) {
        if (p instanceof Window) return (Window) p;
        return p == null ? null : SwingUtilities.getWindowAncestor(p);
    }

    private static void showMessageDialog(Component parent, String title, String message,
                                          Color iconBg, Color iconAccent, Color iconFg, String symbol) {
        JDialog dlg = createModalDialog(parent, title);
        JPanel body = buildMessageBody(title, message, iconBg, iconAccent, iconFg, symbol);

        ModernButton btnOk = new ModernButton("Mengerti", ModernButton.Variant.PRIMARY);
        btnOk.setPreferredSize(new Dimension(120, 40));
        btnOk.addActionListener(e -> dlg.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(btnOk);

        JPanel root = wrapDialogContent(body, footer);
        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(btnOk);
        bindEscape(dlg, dlg::dispose);
        dlg.setMinimumSize(new Dimension(440, 0));
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    static boolean showConfirmDialog(Component parent, String title, String message,
                                     String okText, ModernButton.Variant okVariant) {
        final boolean[] accepted = {false};
        JDialog dlg = createModalDialog(parent, title);

        boolean isDanger = okVariant == ModernButton.Variant.DANGER;
        JPanel body = buildMessageBody(
                title,
                message,
                isDanger ? BADGE_RED_BG : PRIMARY_SOFT_BG,
                isDanger ? BADGE_RED_ACCENT : PRIMARY_START,
                isDanger ? BADGE_RED_TEXT : PRIMARY_END,
                isDanger ? "!" : "?"
        );

        ModernButton btnCancel = new ModernButton("Batal", ModernButton.Variant.SECONDARY);
        btnCancel.setPreferredSize(new Dimension(104, 40));
        btnCancel.addActionListener(e -> dlg.dispose());

        ModernButton btnOk = new ModernButton(okText, okVariant);
        btnOk.setPreferredSize(new Dimension(Math.max(124, btnOk.getPreferredSize().width + 8), 40));
        btnOk.addActionListener(e -> { accepted[0] = true; dlg.dispose(); });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.add(btnCancel);
        footer.add(btnOk);

        JPanel root = wrapDialogContent(body, footer);
        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(btnOk);
        bindEscape(dlg, dlg::dispose);
        dlg.setMinimumSize(new Dimension(460, 0));
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return accepted[0];
    }

    /** Dialog form custom seragam untuk booking, pembayaran, dll. */
    static boolean showFormDialog(Component parent, String title, String subtitle, JComponent content,
                                  String okText, ModernButton.Variant okVariant) {
        final boolean[] accepted = {false};
        JDialog dlg = createModalDialog(parent, title);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(fontHeading(18f));
        lblTitle.setForeground(TEXT_HEADING);
        header.add(lblTitle);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(fontBody(12.5f));
            lblSub.setForeground(TEXT_MUTED);
            header.add(Box.createVerticalStrut(4));
            header.add(lblSub);
        }
        header.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(header, BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);

        ModernButton btnCancel = new ModernButton("Batal", ModernButton.Variant.SECONDARY);
        btnCancel.setPreferredSize(new Dimension(104, 40));
        btnCancel.addActionListener(e -> dlg.dispose());

        ModernButton btnOk = new ModernButton(okText, okVariant);
        btnOk.setPreferredSize(new Dimension(Math.max(132, btnOk.getPreferredSize().width + 10), 40));
        btnOk.addActionListener(e -> { accepted[0] = true; dlg.dispose(); });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.add(btnCancel);
        footer.add(btnOk);

        JPanel root = wrapDialogContent(body, footer);
        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(btnOk);
        bindEscape(dlg, dlg::dispose);
        dlg.setMinimumSize(new Dimension(460, 0));
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return accepted[0];
    }

    /** Dialog pratinjau read-only seragam (misalnya pratinjau struk). */
    static void showPreviewDialog(Component parent, String title, String subtitle, JComponent content) {
        JDialog dlg = createModalDialog(parent, title);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(fontHeading(18f));
        lblTitle.setForeground(TEXT_HEADING);
        header.add(lblTitle);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(fontBody(12f));
            lblSub.setForeground(TEXT_MUTED);
            header.add(Box.createVerticalStrut(4));
            header.add(lblSub);
        }
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(header, BorderLayout.NORTH);
        body.add(content, BorderLayout.CENTER);

        ModernButton btnClose = new ModernButton("Tutup", ModernButton.Variant.PRIMARY);
        btnClose.setPreferredSize(new Dimension(112, 40));
        btnClose.addActionListener(e -> dlg.dispose());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(btnClose);

        JPanel root = wrapDialogContent(body, footer);
        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(btnClose);
        bindEscape(dlg, dlg::dispose);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private static JDialog createModalDialog(Component parent, String title) {
        JDialog dlg = new JDialog(resolveOwner(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(false);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        return dlg;
    }

    private static JPanel buildMessageBody(String title, String message, Color bg, Color accent, Color fg, String symbol) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        final int boxSize = 44;
        final int pad = 2;
        final int circleDia = boxSize - pad * 2;
        JComponent iconCircle = new JComponent() {
            private final Dimension dim = new Dimension(boxSize, boxSize);
            @Override public Dimension getPreferredSize() { return dim; }
            @Override public Dimension getMinimumSize() { return dim; }
            @Override public Dimension getMaximumSize() { return dim; }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                enableAntiAliasing(g2);
                g2.setColor(bg);
                g2.fill(new Ellipse2D.Double(pad, pad, circleDia, circleDia));
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(1.6f));
                g2.draw(new Ellipse2D.Double(pad + 0.8, pad + 0.8, circleDia - 1.6, circleDia - 1.6));
                g2.setFont(fontHeading(18f));
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                int tx = Math.round((boxSize - fm.stringWidth(symbol)) / 2f);
                int ty = Math.round((boxSize - fm.getHeight()) / 2f + fm.getAscent());
                g2.drawString(symbol, tx, ty);
                g2.dispose();
            }
        };
        JPanel iconWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        iconWrap.setOpaque(false);
        iconWrap.add(iconCircle);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(fontHeading(16f));
        lblTitle.setForeground(TEXT_HEADING);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        final int msgWidth = 370;
        JTextArea ta = createWrappedMessageArea(message == null ? "" : message, msgWidth);

        textCol.add(lblTitle);
        textCol.add(Box.createVerticalStrut(8));
        textCol.add(ta);

        row.add(iconWrap, BorderLayout.WEST);
        row.add(textCol, BorderLayout.CENTER);
        return row;
    }

    /** Membuat JTextArea transparan non-editable yang otomatis wrap dan menghitung tinggi akurat. */
    private static JTextArea createWrappedMessageArea(String text, int targetWidth) {
        JTextArea ta = new JTextArea(text);
        ta.setFont(fontBody(13.5f));
        ta.setForeground(TEXT_BODY);
        ta.setOpaque(false);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(null);
        ta.setAlignmentX(Component.LEFT_ALIGNMENT);

        ta.setSize(new Dimension(targetWidth, Short.MAX_VALUE));
        int uiHeight = ta.getPreferredSize().height;
        int fmHeight = computeWrappedTextHeight(ta.getText(), ta.getFontMetrics(ta.getFont()), targetWidth);
        int finalHeight = Math.max(uiHeight, fmHeight) + 6;

        Dimension pref = new Dimension(targetWidth, finalHeight);
        ta.setPreferredSize(pref);
        ta.setMinimumSize(pref);
        return ta;
    }

    private static int computeWrappedTextHeight(String text, FontMetrics fm, int maxWidth) {
        int lineH = Math.max(16, fm.getHeight());
        int safeW = Math.max(120, maxWidth);
        int totalLines = 0;
        String[] paragraphs = (text == null ? "" : text).split("\n", -1);
        for (String para : paragraphs) {
            if (para.isEmpty()) {
                totalLines++;
                continue;
            }
            int curW = 0;
            int paraLines = 1;
            String[] words = para.split(" ", -1);
            int spaceW = fm.stringWidth(" ");
            for (String word : words) {
                int ww = fm.stringWidth(word);
                int gap = curW == 0 ? 0 : spaceW;
                if (curW > 0 && curW + gap + ww > safeW) {
                    paraLines++;
                    curW = ww;
                } else {
                    curW += gap + ww;
                }
                while (curW > safeW) {
                    paraLines++;
                    curW -= safeW;
                }
            }
            totalLines += paraLines;
        }
        return Math.max(lineH, totalLines * lineH + 4);
    }

    private static JPanel wrapDialogContent(JComponent body, JComponent footer) {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(SURFACE_CARD);
        root.setBorder(new EmptyBorder(22, 24, 20, 24));
        root.add(body, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE),
                new EmptyBorder(14, 0, 0, 0)
        ));
        south.add(footer, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);
        return root;
    }

    private static void bindEscape(JDialog dlg, Runnable action) {
        dlg.getRootPane().registerKeyboardAction(
                e -> action.run(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    static String infoBooking(Booking b, LocalDateTime now) {
        if (b.getStatus() == StatusBooking.MENUNGGU_BAYAR) {
            long menit = Math.max(0, Duration.between(now, b.getBatasBayar()).toMinutes());
            return "Bayar sebelum " + b.getBatasBayar().format(Fmt.JAM) + " (sisa " + menit + " menit)";
        }
        if (b.getStatus() == StatusBooking.MENUNGGU_KONFIRMASI) return "Menunggu admin mengonfirmasi";
        if (b.getStatus() == StatusBooking.DIBATALKAN && b.getRefund() > 0) return "Refund " + Fmt.rp(b.getRefund());
        return "";
    }

    /** Mengisi tabel booking; pilihan baris dipertahankan berdasarkan ID booking. */
    static void isiTabelBooking(JTable tabel, DefaultTableModel m, List<Booking> data, boolean admin, LocalDateTime now) {
        String terpilih = idTerpilih(tabel);
        String[] kolom = admin
                ? new String[]{"ID", "Pelanggan", "Lapangan", "Tanggal", "Jam", "Total", "Metode", "Status", "Keterangan"}
                : new String[]{"ID", "Lapangan", "Tanggal", "Jam", "Total", "Metode", "Status", "Keterangan"};
        Object[][] baris = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            Booking b = data.get(i);
            String jam = b.getMulai().format(Fmt.JAM) + " - " + b.getSelesai().format(Fmt.JAM);
            if (admin) baris[i] = new Object[]{b.getId(), b.getPelanggan().getNama(), b.getLapangan().getNama(),
                    b.getMulai().format(Fmt.TANGGAL), jam, Fmt.rp(b.getTotal()), b.getMetode(), b.getStatus(), infoBooking(b, now)};
            else baris[i] = new Object[]{b.getId(), b.getLapangan().getNama(), b.getMulai().format(Fmt.TANGGAL), jam,
                    Fmt.rp(b.getTotal()), b.getMetode(), b.getStatus(), infoBooking(b, now)};
        }
        m.setDataVector(baris, kolom);
        for (int i = 0; i < m.getRowCount() && terpilih != null; i++)
            if (terpilih.equals(m.getValueAt(i, 0))) { tabel.setRowSelectionInterval(i, i); break; }

        int kolTotal = admin ? 5 : 4;
        int kolStatus = admin ? 7 : 6;
        if (admin) {
            setColWidths(tabel, 72, 115, 105, 96, 108, 105, 82, 178, 190);
        } else {
            setColWidths(tabel, 76, 118, 100, 112, 110, 88, 178, 210);
        }
        tabel.getColumnModel().getColumn(kolTotal).setCellRenderer(new ModernCellRenderer(SwingConstants.RIGHT, true));
        tabel.getColumnModel().getColumn(kolStatus).setCellRenderer(new PillBadgeRenderer());
    }

    static String infoUpgrade(UpgradeMember u) {
        if (u.getStatus() == StatusUpgrade.MENUNGGU_KONFIRMASI) return "Menunggu admin mengonfirmasi";
        if (u.getStatus() == StatusUpgrade.LUNAS)
            return u.getWaktuSelesai() != null ? "Aktif sejak " + Fmt.waktu(u.getWaktuSelesai()) : "Lunas";
        if (u.getStatus() == StatusUpgrade.DIBATALKAN)
            return u.getWaktuSelesai() != null ? "Dibatalkan pada " + Fmt.waktu(u.getWaktuSelesai()) : "Dibatalkan";
        return "";
    }

    /** Mengisi tabel pengajuan upgrade member; pilihan baris dipertahankan berdasarkan ID. */
    static void isiTabelUpgrade(JTable tabel, DefaultTableModel m, List<UpgradeMember> data, boolean admin) {
        String terpilih = idTerpilih(tabel);
        String[] kolom = admin
                ? new String[]{"ID", "Pelanggan", "Dari Tier", "Ke Tier", "Biaya", "Metode", "Status", "Waktu Pengajuan", "Keterangan"}
                : new String[]{"ID", "Dari Tier", "Ke Tier", "Biaya", "Metode", "Status", "Waktu Pengajuan", "Keterangan"};
        Object[][] baris = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            UpgradeMember u = data.get(i);
            if (admin) baris[i] = new Object[]{u.getId(), u.getPelanggan().getNama(), u.getDariTier(), u.getKeTier(),
                    Fmt.rp(u.getBiaya()), u.getMetode(), u.getStatus(), Fmt.waktu(u.getWaktuPengajuan()), infoUpgrade(u)};
            else baris[i] = new Object[]{u.getId(), u.getDariTier(), u.getKeTier(),
                    Fmt.rp(u.getBiaya()), u.getMetode(), u.getStatus(), Fmt.waktu(u.getWaktuPengajuan()), infoUpgrade(u)};
        }
        m.setDataVector(baris, kolom);
        for (int i = 0; i < m.getRowCount() && terpilih != null; i++)
            if (terpilih.equals(m.getValueAt(i, 0))) { tabel.setRowSelectionInterval(i, i); break; }

        int kolBiaya = admin ? 4 : 3;
        int kolStatus = admin ? 6 : 5;
        if (admin) {
            setColWidths(tabel, 72, 112, 74, 74, 98, 82, 178, 136, 180);
        } else {
            setColWidths(tabel, 76, 82, 82, 102, 88, 178, 142, 210);
        }
        tabel.getColumnModel().getColumn(kolBiaya).setCellRenderer(new ModernCellRenderer(SwingConstants.RIGHT, true));
        tabel.getColumnModel().getColumn(kolStatus).setCellRenderer(new PillBadgeRenderer());
    }

    static void setColWidths(JTable tabel, int... widths) {
        for (int i = 0; i < widths.length && i < tabel.getColumnModel().getColumnCount(); i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    static String idTerpilih(JTable t) {
        int r = t.getSelectedRow();
        return r < 0 ? null : (String) t.getModel().getValueAt(t.convertRowIndexToModel(r), 0);
    }

    /** Memberi warna pada kolom status (dipertahankan untuk kompatibilitas). */
    static class StatusRenderer extends PillBadgeRenderer {}

    static JPanel panelTombol(JButton... tombol) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setOpaque(false);
        for (JButton b : tombol) p.add(b);
        return p;
    }

    // =========================================================================
    //  HELPER STYLING INPUT & KONTROL STANDAR SWING
    // =========================================================================

    static <T> void styleComboBox(JComboBox<T> cb) {
        cb.setUI(new ModernComboBoxUI());
        cb.setOpaque(false);
        cb.setFont(fontBody(13f));
        cb.setForeground(TEXT_HEADING);
        cb.setBackground(SURFACE_CARD);
        cb.setBorder(new EmptyBorder(2, 6, 2, 2));
        cb.setRenderer(new ModernComboRenderer<>());
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setMaximumRowCount(8);
        cb.setPreferredSize(new Dimension(Math.max(150, cb.getPreferredSize().width + 24), 38));
        cb.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { cb.repaint(); }
            @Override public void focusLost(FocusEvent e) { cb.repaint(); }
        });
    }

    static void styleDateSpinner(JSpinner sp) {
        sp.setUI(new ModernSpinnerUI());
        sp.setOpaque(false);
        sp.setFont(fontLabel(13f));
        sp.setForeground(TEXT_HEADING);
        sp.setBackground(SURFACE_CARD);
        sp.setBorder(new EmptyBorder(2, 4, 2, 2));
        sp.setPreferredSize(new Dimension(142, 38));

        JComponent editor = sp.getEditor();
        if (editor != null) {
            editor.setOpaque(false);
        }
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setOpaque(false);
            tf.setFont(fontLabel(13f));
            tf.setForeground(TEXT_HEADING);
            tf.setDisabledTextColor(TEXT_MUTED);
            tf.setBackground(SURFACE_CARD);
            tf.setCaretColor(PRIMARY_END);
            tf.setSelectionColor(PRIMARY_SOFT_ALT);
            tf.setSelectedTextColor(PRIMARY_HOVER_END);
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setBorder(new EmptyBorder(4, 8, 4, 6));
            tf.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { sp.repaint(); }
                @Override public void focusLost(FocusEvent e) { sp.repaint(); }
            });
        }
    }

    /** Menggambar ikon chevron anti-aliased ke atas atau bawah. */
    private static void paintChevron(Graphics2D g2, int cx, int cy, int halfW, int halfH, boolean up, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        enableAntiAliasing(g);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        if (up) {
            p.moveTo(cx - halfW, cy + halfH);
            p.lineTo(cx, cy - halfH);
            p.lineTo(cx + halfW, cy + halfH);
        } else {
            p.moveTo(cx - halfW, cy - halfH);
            p.lineTo(cx, cy + halfH);
            p.lineTo(cx + halfW, cy - halfH);
        }
        g.draw(p);
        g.dispose();
    }

    /** UI kustom JComboBox bertema soft light membulat dengan tombol panah halus dan popup terang. */
    private static class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton() {
                private boolean hovered;
                {
                    setOpaque(false);
                    setContentAreaFilled(false);
                    setBorderPainted(false);
                    setFocusPainted(false);
                    setFocusable(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setPreferredSize(new Dimension(28, 32));
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                        @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    enableAntiAliasing(g2);
                    int w = getWidth();
                    int h = getHeight();
                    boolean enabled = comboBox != null && comboBox.isEnabled();
                    if (enabled && (hovered || (comboBox != null && isPopupVisible(comboBox)))) {
                        g2.setColor(PRIMARY_SOFT_BG);
                        g2.fill(new RoundRectangle2D.Double(2, 3, w - 5, h - 6, 7, 7));
                    }
                    Color chevronCol = !enabled
                            ? BADGE_GRAY_TEXT
                            : ((hovered || (comboBox != null && isPopupVisible(comboBox))) ? PRIMARY_HOVER_END : TEXT_BODY);
                    paintChevron(g2, w / 2, h / 2 + 1, 4, 2, false, chevronCol);
                    g2.dispose();
                }
            };
            return btn;
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane sp = super.createScroller();
                    styleScrollPane(sp);
                    sp.setBorder(null);
                    return sp;
                }
            };
            popup.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE, 1));
            popup.setBackground(SURFACE_CARD);

            JList<Object> list = popup.getList();
            list.setBackground(SURFACE_CARD);
            list.setForeground(TEXT_HEADING);
            list.setSelectionBackground(PRIMARY_SOFT_BG);
            list.setSelectionForeground(PRIMARY_HOVER_END);
            list.setFixedCellHeight(34);
            list.putClientProperty(PROP_COMBO_HOVER, -1);

            list.addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int idx = list.locationToIndex(e.getPoint());
                    Integer prev = (Integer) list.getClientProperty(PROP_COMBO_HOVER);
                    if (prev == null || prev != idx) {
                        list.putClientProperty(PROP_COMBO_HOVER, idx);
                        list.repaint();
                    }
                }
            });
            list.addMouseListener(new MouseAdapter() {
                @Override public void mouseExited(MouseEvent e) {
                    list.putClientProperty(PROP_COMBO_HOVER, -1);
                    list.repaint();
                }
            });
            return popup;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = c.getWidth();
            int h = c.getHeight();
            boolean enabled = comboBox.isEnabled();
            boolean active = enabled && (comboBox.isFocusOwner() || isPopupVisible(comboBox));

            if (active) {
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 42));
                g2.fill(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12));
            }

            RoundRectangle2D box = new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, 10, 10);
            g2.setColor(enabled ? SURFACE_CARD : SURFACE_SUBTLE);
            g2.fill(box);

            g2.setColor(active ? BORDER_FOCUS : BORDER_INPUT);
            g2.setStroke(new BasicStroke(active ? 1.55f : 1.1f));
            g2.draw(box);
            g2.dispose();

            Rectangle r = rectangleForCurrentValue();
            paintCurrentValue(g, r, false);
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Latar membulat sudah digambar bersih pada paint()
        }
    }

    /** Renderer dropdown JComboBox terang dengan padding lega dan highlight pastel teal. */
    private static class ModernComboRenderer<T> extends JLabel implements ListCellRenderer<T> {
        ModernComboRenderer() {
            setOpaque(true);
            setFont(fontBody(13f));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends T> list, T value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value == null ? "" : value.toString());
            if (index == -1) {
                setOpaque(false);
                setBorder(new EmptyBorder(0, 6, 0, 4));
                setFont(fontBody(13f));
                setForeground(list.isEnabled() ? TEXT_HEADING : BADGE_GRAY_TEXT);
                return this;
            }
            Object hoverObj = list.getClientProperty(PROP_COMBO_HOVER);
            int hoverIdx = hoverObj instanceof Integer ? (Integer) hoverObj : -1;
            boolean highlighted = isSelected || index == hoverIdx;

            setOpaque(true);
            setBorder(new EmptyBorder(7, 12, 7, 12));
            if (highlighted) {
                setBackground(PRIMARY_SOFT_BG);
                setForeground(PRIMARY_HOVER_END);
                setFont(fontLabel(13f));
            } else {
                setBackground(SURFACE_CARD);
                setForeground(TEXT_HEADING);
                setFont(fontBody(13f));
            }
            return this;
        }
    }

    /** UI kustom JSpinner bertema soft light membulat dengan tombol panah atas/bawah terang & chevron rapi. */
    private static class ModernSpinnerUI extends BasicSpinnerUI {
        private boolean isSpinnerFocused() {
            if (spinner == null) return false;
            if (spinner.isFocusOwner()) return true;
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                return ((JSpinner.DefaultEditor) editor).getTextField().isFocusOwner();
            }
            return editor != null && editor.isFocusOwner();
        }

        @Override
        protected Component createNextButton() {
            JButton btn = createChevronButton(true);
            btn.setName("Spinner.nextButton");
            installNextButtonListeners(btn);
            return btn;
        }

        @Override
        protected Component createPreviousButton() {
            JButton btn = createChevronButton(false);
            btn.setName("Spinner.previousButton");
            installPreviousButtonListeners(btn);
            return btn;
        }

        private JButton createChevronButton(boolean up) {
            return new JButton() {
                private boolean hovered;
                {
                    setOpaque(false);
                    setContentAreaFilled(false);
                    setBorderPainted(false);
                    setFocusPainted(false);
                    setFocusable(false);
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setPreferredSize(new Dimension(24, 17));
                    addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                        @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    enableAntiAliasing(g2);
                    int w = getWidth();
                    int h = getHeight();
                    boolean enabled = spinner != null && spinner.isEnabled();
                    boolean pressed = getModel().isArmed() && getModel().isPressed();

                    if (enabled && (pressed || hovered)) {
                        g2.setColor(pressed ? PRIMARY_SOFT_ALT : PRIMARY_SOFT_BG);
                        g2.fill(new RoundRectangle2D.Double(2, up ? 2 : 1, w - 4, h - 3, 6, 6));
                    }

                    // Garis pemisah tipis antara area teks tanggal dan tombol panah
                    g2.setColor(BORDER_SUBTLE);
                    g2.drawLine(0, up ? 3 : 0, 0, up ? h : h - 3);
                    if (up) {
                        g2.drawLine(1, h - 1, w - 3, h - 1);
                    }

                    Color col = !enabled ? BADGE_GRAY_TEXT : (hovered || pressed ? PRIMARY_HOVER_END : TEXT_BODY);
                    paintChevron(g2, w / 2, h / 2 + (up ? 0 : 0), 3, 2, up, col);
                    g2.dispose();
                }
            };
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = c.getWidth();
            int h = c.getHeight();
            boolean enabled = c.isEnabled();
            boolean focused = enabled && isSpinnerFocused();

            if (focused) {
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 42));
                g2.fill(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 12, 12));
            }

            RoundRectangle2D box = new RoundRectangle2D.Double(1.5, 1.5, w - 3, h - 3, 10, 10);
            g2.setColor(enabled ? SURFACE_CARD : SURFACE_SUBTLE);
            g2.fill(box);

            g2.setColor(focused ? BORDER_FOCUS : BORDER_INPUT);
            g2.setStroke(new BasicStroke(focused ? 1.55f : 1.1f));
            g2.draw(box);
            g2.dispose();
        }
    }

    static JPanel createPageHeader(String title, String subtitle, JComponent rightAccessory) {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(fontHeading(22f));
        lblTitle.setForeground(TEXT_HEADING);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(fontBody(13f));
        lblSub.setForeground(TEXT_MUTED);

        left.add(lblTitle);
        left.add(Box.createVerticalStrut(3));
        left.add(lblSub);

        header.add(left, BorderLayout.WEST);
        if (rightAccessory != null) {
            JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
            rightWrap.setOpaque(false);
            rightWrap.add(rightAccessory);
            header.add(rightWrap, BorderLayout.EAST);
        }
        return header;
    }

    static PillLabel createAutoRefreshBadge() {
        return new PillLabel("Auto-refresh 30 dtk", PRIMARY_SOFT_BG, PRIMARY_START, PRIMARY_END, true);
    }

    static PillLabel createMemberBadge(Member m) {
        if (m == Member.GOLD) {
            return new PillLabel("GOLD", BADGE_AMBER_BG, BADGE_AMBER_ACCENT, BADGE_AMBER_TEXT, true);
        }
        if (m == Member.SILVER) {
            return new PillLabel("SILVER", BADGE_BLUE_BG, BADGE_BLUE_ACCENT, BADGE_BLUE_TEXT, true);
        }
        return new PillLabel("BIASA", BADGE_GRAY_BG, TEXT_MUTED, TEXT_BODY, true);
    }

    static void updateMemberBadge(PillLabel badge, Member m) {
        if (m == Member.GOLD) {
            badge.update("GOLD", BADGE_AMBER_BG, BADGE_AMBER_ACCENT, BADGE_AMBER_TEXT);
        } else if (m == Member.SILVER) {
            badge.update("SILVER", BADGE_BLUE_BG, BADGE_BLUE_ACCENT, BADGE_BLUE_TEXT);
        } else {
            badge.update("BIASA", BADGE_GRAY_BG, TEXT_MUTED, TEXT_BODY);
        }
    }

    // =========================================================================
    //  KOMPONEN MODERN REUSABLE (Standar Skill swing-soft-modern-ui)
    // =========================================================================

    /** Emblem vektor lapangan olahraga (Graphics2D murni, seragam dengan LoginFrame). */
    static final class CourtEmblem extends JComponent {
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
            enableAntiAliasing(g2);

            int arc = Math.round(size * 0.30f);
            g2.setPaint(new GradientPaint(0, 0, PRIMARY_START, size, size, PRIMARY_END));
            g2.fill(new RoundRectangle2D.Double(0, 0, size, size, arc, arc));

            double pad = size * 0.20;
            double cw = size - pad * 2;
            double ch = size * 0.44;
            double cy = (size - ch) / 2.0;

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(Math.max(1.4f, size * 0.045f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(pad, cy, cw, ch, 4, 4));

            double midX = size / 2.0;
            double midY = size / 2.0;
            g2.drawLine((int) Math.round(midX), (int) Math.round(cy), (int) Math.round(midX), (int) Math.round(cy + ch));
            double cr = ch * 0.28;
            g2.draw(new Ellipse2D.Double(midX - cr, midY - cr, cr * 2, cr * 2));

            double boxW = cw * 0.16;
            double boxH = ch * 0.50;
            g2.draw(new Arc2D.Double(pad - boxW / 2, midY - boxH / 2, boxW, boxH, -90, 180, Arc2D.OPEN));
            g2.draw(new Arc2D.Double(pad + cw - boxW / 2, midY - boxH / 2, boxW, boxH, 90, 180, Arc2D.OPEN));

            g2.dispose();
        }
    }

    /** Lencana kapsul pastel (pill badge) dengan titik aksen opsional. */
    static class PillLabel extends JLabel {
        private Color pillBg;
        private Color dotColor;
        private final boolean showDot;

        PillLabel(String text, Color bg, Color dotColor, Color fg, boolean showDot) {
            super(text);
            this.pillBg = bg;
            this.dotColor = dotColor;
            this.showDot = showDot;
            setOpaque(false);
            setFont(fontLabel(11.5f));
            setForeground(fg);
            setBorder(new EmptyBorder(4, showDot ? 20 : 11, 4, 11));
        }

        void update(String text, Color bg, Color dotColor, Color fg) {
            setText(text);
            this.pillBg = bg;
            this.dotColor = dotColor;
            setForeground(fg);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(pillBg);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
            if (showDot && dotColor != null) {
                g2.setColor(dotColor);
                g2.fill(new Ellipse2D.Double(8, (h - 6) / 2.0, 6, 6));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Sidebar navigasi kiri terang (#FFFFFF) dengan pemisah kanan #E2E8F0, branding atas, dan kartu akun bawah. */
    static class AppSidebar extends JPanel {
        private final JPanel navList = new JPanel();
        private final JLabel lblAccountName = new JLabel();
        private final JLabel lblAccountSub = new JLabel();
        private final PillLabel badgeRole;
        private final SidebarNavButton btnLogout = new SidebarNavButton("Logout", NavIcon.LOGOUT);

        AppSidebar(String portalLabel, String accountName, String accountSub, PillLabel badgeRole, Runnable onLogout) {
            this.badgeRole = badgeRole;
            setPreferredSize(new Dimension(246, 0));
            setBackground(SURFACE_CARD);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_SUBTLE));

            // Header branding ArenaBook
            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
            top.setBorder(new EmptyBorder(20, 18, 18, 18));
            top.add(new CourtEmblem(36));
            top.add(Box.createHorizontalStrut(11));

            JPanel titles = new JPanel();
            titles.setOpaque(false);
            titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
            JLabel brand = new JLabel("ArenaBook");
            brand.setFont(fontHeading(17f));
            brand.setForeground(TEXT_HEADING);
            JLabel sub = new JLabel(portalLabel);
            sub.setFont(fontBody(11.5f));
            sub.setForeground(PRIMARY_END);
            titles.add(brand);
            titles.add(sub);
            top.add(titles);

            // Area daftar menu tengah
            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.setBorder(new EmptyBorder(6, 12, 12, 12));

            JLabel menuCap = new JLabel("MENU NAVIGASI");
            menuCap.setFont(fontLabel(10.5f));
            menuCap.setForeground(BADGE_GRAY_TEXT);
            menuCap.setBorder(new EmptyBorder(0, 12, 8, 8));

            navList.setOpaque(false);
            navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));

            center.add(menuCap, BorderLayout.NORTH);
            center.add(navList, BorderLayout.CENTER);

            // Area akun aktif & logout di bawah
            JPanel bottom = new JPanel();
            bottom.setOpaque(false);
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
            bottom.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE),
                    new EmptyBorder(14, 14, 16, 14)
            ));

            JPanel accountCard = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    enableAntiAliasing(g2);
                    g2.setColor(SURFACE_SIDEBAR);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(BORDER_SUBTLE);
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, 12, 12));
                    g2.dispose();
                }
            };
            accountCard.setOpaque(false);
            accountCard.setLayout(new BoxLayout(accountCard, BoxLayout.Y_AXIS));
            accountCard.setBorder(new EmptyBorder(11, 12, 11, 12));
            accountCard.setAlignmentX(Component.LEFT_ALIGNMENT);

            lblAccountName.setText(accountName);
            lblAccountName.setFont(fontLabel(13.5f));
            lblAccountName.setForeground(TEXT_HEADING);
            lblAccountName.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            badgeRow.setOpaque(false);
            badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            badgeRow.add(badgeRole);

            lblAccountSub.setText(accountSub);
            lblAccountSub.setFont(fontBody(11.5f));
            lblAccountSub.setForeground(TEXT_MUTED);
            lblAccountSub.setAlignmentX(Component.LEFT_ALIGNMENT);

            accountCard.add(lblAccountName);
            accountCard.add(Box.createVerticalStrut(6));
            accountCard.add(badgeRow);
            if (accountSub != null && !accountSub.isEmpty()) {
                accountCard.add(Box.createVerticalStrut(5));
                accountCard.add(lblAccountSub);
            }

            btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnLogout.addActionListener(e -> onLogout.run());

            bottom.add(accountCard);
            bottom.add(Box.createVerticalStrut(10));
            bottom.add(btnLogout);

            add(top, BorderLayout.NORTH);
            add(center, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);
        }

        void addNavButton(SidebarNavButton btn) {
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            navList.add(btn);
            navList.add(Box.createVerticalStrut(4));
        }

        PillLabel getRoleBadge() { return badgeRole; }

        void setAccountSubtitle(String subtitle) {
            lblAccountSub.setText(subtitle);
        }
    }

    /**
     * JTabbedPane tanpa bilah tab (visual identik CardLayout) sehingga dikendalikan sepenuhnya
     * oleh SidebarNavButton sekaligus tetap kompatibel 100% dengan GuiSmoke.
     */
    static class TablessCardPane extends JTabbedPane {
        TablessCardPane() {
            setOpaque(false);
            setBorder(null);
            setUI(new BasicTabbedPaneUI() {
                @Override protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) { return 0; }
                @Override protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount, int maxTabWidth) { return 0; }
                @Override protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {}
                @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
                @Override protected Insets getContentBorderInsets(int tabPlacement) { return new Insets(0, 0, 0, 0); }
                @Override protected Insets getTabAreaInsets(int tabPlacement) { return new Insets(0, 0, 0, 0); }
            });
        }
    }

    /** Panel latar dengan gradasi terang halus dan aksen geometris/gelombang transparan lembut. */
    static class GradientCanvasPanel extends JPanel {
        private final Color startColor;
        private final Color endColor;
        private final boolean decorativeAccents;

        GradientCanvasPanel() {
            this(BG_CANVAS_START, BG_CANVAS_END, true);
        }

        GradientCanvasPanel(Color startColor, Color endColor) {
            this(startColor, endColor, true);
        }

        GradientCanvasPanel(Color startColor, Color endColor, boolean decorativeAccents) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.decorativeAccents = decorativeAccents;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);
            int w = getWidth();
            int h = getHeight();

            g2.setPaint(new GradientPaint(0, 0, startColor, w, h, endColor));
            g2.fillRect(0, 0, w, h);

            if (decorativeAccents) {
                double base = Math.max(480.0, Math.min(w, h));

                double trBig = base * 0.56;
                double trSmall = base * 0.32;
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 22));
                g2.fill(new Ellipse2D.Double(w - trBig * 0.68, -trBig * 0.30, trBig, trBig));
                g2.setColor(new Color(0x0D, 0x94, 0x88, 14));
                g2.fill(new Ellipse2D.Double(w - trSmall * 0.72, trBig * 0.22, trSmall, trSmall));

                double blBig = base * 0.52;
                double blSmall = base * 0.28;
                g2.setColor(new Color(0x0E, 0xA5, 0xE9, 16));
                g2.fill(new Ellipse2D.Double(-blBig * 0.34, h - blBig * 0.70, blBig, blBig));
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 18));
                g2.fill(new Ellipse2D.Double(blBig * 0.14, h - blSmall * 0.78, blSmall, blSmall));

                double tlRing = base * 0.34;
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 16));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new Ellipse2D.Double(w * 0.06, -tlRing * 0.45, tlRing, tlRing));

                Path2D wave = new Path2D.Double();
                wave.moveTo(0, h * 0.83);
                wave.curveTo(w * 0.26, h * 0.74, w * 0.62, h * 0.96, w, h * 0.80);
                wave.lineTo(w, h);
                wave.lineTo(0, h);
                wave.closePath();
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 13));
                g2.fill(wave);

                Path2D wave2 = new Path2D.Double();
                wave2.moveTo(0, h * 0.90);
                wave2.curveTo(w * 0.34, h * 0.98, w * 0.68, h * 0.82, w, h * 0.89);
                wave2.lineTo(w, h);
                wave2.lineTo(0, h);
                wave2.closePath();
                g2.setColor(new Color(0x0D, 0x94, 0x88, 10));
                g2.fill(wave2);
            }

            g2.dispose();
        }
    }

    /** Panel kartu putih bersudut membulat dengan soft drop shadow bertingkat dan border tipis. */
    static class ShadowCardPanel extends JPanel {
        private final int arc;
        private final int shadowSize;
        private Color cardColor = SURFACE_CARD;
        private Color borderColor = BORDER_SUBTLE;

        ShadowCardPanel() {
            this(16, 8);
        }

        ShadowCardPanel(int arc, int shadowSize) {
            this.arc = arc;
            this.shadowSize = shadowSize;
            setOpaque(false);
            setBorder(new EmptyBorder(shadowSize, shadowSize, shadowSize, shadowSize));
        }

        void setCardColor(Color cardColor) {
            this.cardColor = cardColor;
            repaint();
        }

        void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        void setContentPadding(int top, int left, int bottom, int right) {
            setBorder(new EmptyBorder(shadowSize + top, shadowSize + left, shadowSize + bottom, shadowSize + right));
        }

        int getArc() { return arc; }
        int getShadowSize() { return shadowSize; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();
            int s = shadowSize;
            int cardW = w - s * 2;
            int cardH = h - s * 2;

            if (cardW > 0 && cardH > 0) {
                int layers = Math.min(5, Math.max(1, s / 2));
                for (int i = layers; i >= 1; i--) {
                    float ratio = (float) i / layers;
                    int alpha = Math.max(2, Math.round(13 * (1f - ratio * 0.72f)));
                    int spread = Math.round(i * 1.5f);
                    int yOffset = Math.round(i * 1.3f);
                    g2.setColor(new Color(15, 23, 42, alpha));
                    g2.fill(new RoundRectangle2D.Double(
                            s - spread / 2.0,
                            s - spread / 2.0 + yOffset,
                            cardW + spread,
                            cardH + spread,
                            arc + spread,
                            arc + spread
                    ));
                }

                RoundRectangle2D cardRect = new RoundRectangle2D.Double(s, s, cardW, cardH, arc, arc);
                g2.setColor(cardColor);
                g2.fill(cardRect);

                if (borderColor != null) {
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Double(s + 0.5, s + 0.5, cardW - 1, cardH - 1, arc, arc));
                }
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tombol modern membulat dengan gradasi halus, cursor tangan, dan efek hover/pressed. */
    static class ModernButton extends JButton {
        enum Variant { PRIMARY, SECONDARY, DANGER, DANGER_SOFT, GHOST }

        private Variant variant;
        private final int arc;
        private boolean hovered;

        ModernButton(String text) {
            this(text, Variant.PRIMARY);
        }

        ModernButton(String text, Variant variant) {
            this(text, variant, 11);
        }

        ModernButton(String text, Variant variant, int arc) {
            super(text);
            this.variant = variant;
            this.arc = arc;
            setFont(fontLabel(13f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setMargin(new Insets(9, 16, 9, 16));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            applyForeground();

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setVariant(Variant variant) {
            this.variant = variant;
            applyForeground();
            repaint();
        }

        private void applyForeground() {
            switch (variant) {
                case PRIMARY:
                case DANGER:
                    setForeground(Color.WHITE);
                    break;
                case DANGER_SOFT:
                    setForeground(DANGER_END);
                    break;
                case GHOST:
                    setForeground(PRIMARY_END);
                    break;
                case SECONDARY:
                default:
                    setForeground(TEXT_BODY);
                    break;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();
            boolean pressed = getModel().isArmed() && getModel().isPressed();
            boolean enabled = isEnabled();

            RoundRectangle2D shape = new RoundRectangle2D.Double(1, 1, w - 2, h - 2, arc, arc);

            if (!enabled) {
                g2.setColor(SURFACE_SUBTLE);
                g2.fill(shape);
                g2.setColor(BORDER_SUBTLE);
                g2.draw(shape);
                setForeground(BADGE_GRAY_TEXT);
            } else {
                applyForeground();
                switch (variant) {
                    case PRIMARY:
                        if (pressed) {
                            g2.setColor(PRIMARY_PRESSED);
                        } else if (hovered) {
                            g2.setPaint(new GradientPaint(0, 0, PRIMARY_HOVER_START, w, h, PRIMARY_HOVER_END));
                        } else {
                            g2.setPaint(new GradientPaint(0, 0, PRIMARY_START, w, h, PRIMARY_END));
                        }
                        g2.fill(shape);
                        break;

                    case DANGER:
                        if (pressed) {
                            g2.setColor(DANGER_PRESSED);
                        } else if (hovered) {
                            g2.setPaint(new GradientPaint(0, 0, DANGER_END, w, h, DANGER_PRESSED));
                        } else {
                            g2.setPaint(new GradientPaint(0, 0, DANGER_START, w, h, DANGER_END));
                        }
                        g2.fill(shape);
                        break;

                    case DANGER_SOFT:
                        g2.setColor(pressed ? BADGE_RED_BG : (hovered ? new Color(0xFF, 0xE4, 0xE6) : DANGER_SOFT_BG));
                        g2.fill(shape);
                        g2.setColor(DANGER_SOFT_BORDER);
                        g2.draw(shape);
                        break;

                    case GHOST:
                        if (pressed) {
                            g2.setColor(PRIMARY_SOFT_ALT);
                            g2.fill(shape);
                        } else if (hovered) {
                            g2.setColor(PRIMARY_SOFT_BG);
                            g2.fill(shape);
                        }
                        break;

                    case SECONDARY:
                    default:
                        g2.setColor(pressed ? BORDER_SUBTLE : (hovered ? SURFACE_SUBTLE : SURFACE_CARD));
                        g2.fill(shape);
                        g2.setColor(hovered ? TEXT_MUTED : BORDER_INPUT);
                        g2.draw(shape);
                        break;
                }
            }

            if (isFocusOwner() && enabled) {
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 90));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, arc, arc));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Jenis ikon vektor kiri untuk ModernTextField / ModernPasswordField. */
    enum InputIcon { NONE, USER, LOCK, BADGE, SEARCH }

    static void paintVectorIcon(Graphics2D g2, InputIcon icon, int cx, int cy, int size, Color color) {
        if (icon == null || icon == InputIcon.NONE) return;
        Graphics2D g = (Graphics2D) g2.create();
        enableAntiAliasing(g);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        double r = size / 2.0;
        switch (icon) {
            case USER:
                double headR = size * 0.23;
                g.draw(new Ellipse2D.Double(cx - headR, cy - r + 1.5, headR * 2, headR * 2));
                g.draw(new Arc2D.Double(cx - r + 1.5, cy + 0.5, size - 3, size * 0.72, 15, 150, Arc2D.OPEN));
                break;

            case LOCK:
                double bw = size * 0.76;
                double bh = size * 0.52;
                g.draw(new RoundRectangle2D.Double(cx - bw / 2, cy - 0.5, bw, bh, 4, 4));
                double sw = size * 0.46;
                g.draw(new Arc2D.Double(cx - sw / 2, cy - r + 1.2, sw, size * 0.56, 0, 180, Arc2D.OPEN));
                g.fill(new Ellipse2D.Double(cx - 1.3, cy + bh * 0.34, 2.6, 2.6));
                break;

            case BADGE:
                double cw = size * 0.92;
                double ch = size * 0.72;
                g.draw(new RoundRectangle2D.Double(cx - cw / 2, cy - ch / 2, cw, ch, 4, 4));
                g.fill(new Ellipse2D.Double(cx - cw * 0.30, cy - 2.2, 3.6, 3.6));
                g.drawLine((int) Math.round(cx - 0.5), cy - 2, (int) Math.round(cx + cw * 0.30), cy - 2);
                g.drawLine((int) Math.round(cx - 0.5), cy + 2, (int) Math.round(cx + cw * 0.22), cy + 2);
                break;

            case SEARCH:
                double sr = size * 0.32;
                g.draw(new Ellipse2D.Double(cx - r + 1.5, cy - r + 1.5, sr * 2, sr * 2));
                g.drawLine((int) Math.round(cx + 1.5), (int) Math.round(cy + 1.5),
                        (int) Math.round(cx + r - 1.5), (int) Math.round(cy + r - 1.5));
                break;

            default:
                break;
        }
        g.dispose();
    }

    private static void paintEyeIcon(Graphics2D g2, int cx, int cy, boolean visible, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        enableAntiAliasing(g);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D eye = new Path2D.Double();
        eye.moveTo(cx - 7.5, cy);
        eye.quadTo(cx, cy - 6.2, cx + 7.5, cy);
        eye.quadTo(cx, cy + 6.2, cx - 7.5, cy);
        g.draw(eye);

        g.draw(new Ellipse2D.Double(cx - 2.3, cy - 2.3, 4.6, 4.6));
        if (!visible) {
            g.drawLine(cx - 6, cy + 5, cx + 6, cy - 5);
        }
        g.dispose();
    }

    /** Input teks modern membulat dengan placeholder, ikon vektor kiri, dan focus ring teal. */
    static class ModernTextField extends JTextField {
        private final String placeholder;
        private final InputIcon icon;
        private final int arc = 11;
        private boolean focused;

        ModernTextField(String placeholder, InputIcon icon) {
            super();
            this.placeholder = placeholder;
            this.icon = icon == null ? InputIcon.NONE : icon;
            setUI(new BasicTextFieldUI() {
                @Override protected void paintBackground(Graphics g) {}
            });
            setOpaque(false);
            setFont(fontBody(14f));
            setForeground(TEXT_HEADING);
            setCaretColor(PRIMARY_END);
            int leftPad = this.icon == InputIcon.NONE ? 14 : 42;
            setBorder(new EmptyBorder(10, leftPad, 10, 14));
            setPreferredSize(new Dimension(280, 46));

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();

            if (focused) {
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 45));
                g2.fill(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, arc + 3, arc + 3));
            }

            RoundRectangle2D fieldRect = new RoundRectangle2D.Double(2.5, 2.5, w - 5, h - 5, arc, arc);
            g2.setColor(isEnabled() ? SURFACE_CARD : SURFACE_SUBTLE);
            g2.fill(fieldRect);

            g2.setColor(focused ? BORDER_FOCUS : BORDER_INPUT);
            g2.setStroke(new BasicStroke(focused ? 1.75f : 1.1f));
            g2.draw(fieldRect);

            if (icon != InputIcon.NONE) {
                paintVectorIcon(g2, icon, 21, h / 2, 15, focused ? PRIMARY_END : TEXT_MUTED);
            }

            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D gp = (Graphics2D) g.create();
                enableAntiAliasing(gp);
                gp.setFont(getFont());
                gp.setColor(BADGE_GRAY_TEXT);
                Insets in = getInsets();
                FontMetrics fm = gp.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, in.left, textY);
                gp.dispose();
            }
        }
    }

    /** Input password modern membulat dengan ikon gembok, toggle tampilkan/sembunyikan, placeholder, dan focus ring teal. */
    static class ModernPasswordField extends JPasswordField {
        private final String placeholder;
        private final InputIcon icon;
        private final int arc = 11;
        private final char defaultEcho = '\u2022';
        private boolean focused;
        private boolean passwordVisible;
        private boolean eyeHovered;

        ModernPasswordField(String placeholder) {
            this(placeholder, InputIcon.LOCK);
        }

        ModernPasswordField(String placeholder, InputIcon icon) {
            super();
            this.placeholder = placeholder;
            this.icon = icon == null ? InputIcon.NONE : icon;
            setUI(new BasicPasswordFieldUI() {
                @Override protected void paintBackground(Graphics g) {}
            });
            setOpaque(false);
            setEchoChar(defaultEcho);
            setFont(fontBody(14f));
            setForeground(TEXT_HEADING);
            setCaretColor(PRIMARY_END);
            int leftPad = this.icon == InputIcon.NONE ? 14 : 42;
            setBorder(new EmptyBorder(10, leftPad, 10, 42));
            setPreferredSize(new Dimension(280, 46));

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(FocusEvent e) { focused = false; repaint(); }
            });

            MouseAdapter eyeHandler = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    boolean over = isOverEyeToggle(e.getPoint());
                    if (over != eyeHovered) {
                        eyeHovered = over;
                        setCursor(Cursor.getPredefinedCursor(over ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
                        repaint();
                    }
                }
                @Override public void mouseExited(MouseEvent e) {
                    if (eyeHovered) {
                        eyeHovered = false;
                        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                        repaint();
                    }
                }
                @Override public void mousePressed(MouseEvent e) {
                    if (isOverEyeToggle(e.getPoint())) {
                        togglePasswordVisibility();
                        e.consume();
                    }
                }
            };
            addMouseListener(eyeHandler);
            addMouseMotionListener(eyeHandler);
        }

        private boolean isOverEyeToggle(Point p) {
            int w = getWidth();
            return p.x >= w - 36 && p.x <= w - 4 && p.y >= 4 && p.y <= getHeight() - 4;
        }

        void togglePasswordVisibility() {
            passwordVisible = !passwordVisible;
            setEchoChar(passwordVisible ? (char) 0 : defaultEcho);
            repaint();
        }

        void resetVisibility() {
            if (passwordVisible) {
                passwordVisible = false;
                setEchoChar(defaultEcho);
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();

            if (focused) {
                g2.setColor(new Color(0x14, 0xB8, 0xA6, 45));
                g2.fill(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, arc + 3, arc + 3));
            }

            RoundRectangle2D fieldRect = new RoundRectangle2D.Double(2.5, 2.5, w - 5, h - 5, arc, arc);
            g2.setColor(isEnabled() ? SURFACE_CARD : SURFACE_SUBTLE);
            g2.fill(fieldRect);

            g2.setColor(focused ? BORDER_FOCUS : BORDER_INPUT);
            g2.setStroke(new BasicStroke(focused ? 1.75f : 1.1f));
            g2.draw(fieldRect);

            if (icon != InputIcon.NONE) {
                paintVectorIcon(g2, icon, 21, h / 2, 15, focused ? PRIMARY_END : TEXT_MUTED);
            }

            int eyeCx = w - 21;
            int eyeCy = h / 2;
            if (eyeHovered) {
                g2.setColor(PRIMARY_SOFT_BG);
                g2.fill(new RoundRectangle2D.Double(eyeCx - 12, eyeCy - 12, 24, 24, 8, 8));
            }
            paintEyeIcon(g2, eyeCx, eyeCy, passwordVisible, (eyeHovered || passwordVisible) ? PRIMARY_END : TEXT_MUTED);

            g2.dispose();
            super.paintComponent(g);

            if (getPassword().length == 0 && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D gp = (Graphics2D) g.create();
                enableAntiAliasing(gp);
                gp.setFont(getFont());
                gp.setColor(BADGE_GRAY_TEXT);
                Insets in = getInsets();
                FontMetrics fm = gp.getFontMetrics();
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                gp.drawString(placeholder, in.left, textY);
                gp.dispose();
            }
        }
    }

    /** Jenis ikon sederhana untuk tombol navigasi sidebar. */
    enum NavIcon { CALENDAR, LIST, CROWN, CHART, COURT, LOGOUT }

    /** Tombol navigasi sidebar membulat dengan ikon vektor, hover halus, dan state aktif pastel teal. */
    static class SidebarNavButton extends JButton {
        private final NavIcon navIcon;
        private boolean active;
        private boolean hovered;

        SidebarNavButton(String text, NavIcon navIcon) {
            super(text);
            this.navIcon = navIcon;
            setFont(fontLabel(13.5f));
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(10, 42, 10, 14));
            setForeground(navIcon == NavIcon.LOGOUT ? DANGER_END : TEXT_BODY);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? PRIMARY_END : (navIcon == NavIcon.LOGOUT ? DANGER_END : TEXT_BODY));
            repaint();
        }

        boolean isActive() { return active; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            int w = getWidth();
            int h = getHeight();

            if (active) {
                g2.setColor(PRIMARY_SOFT_BG);
                g2.fill(new RoundRectangle2D.Double(2, 1, w - 4, h - 2, 10, 10));
                g2.setColor(PRIMARY_END);
                g2.fill(new RoundRectangle2D.Double(2, 8, 3.5, h - 16, 3, 3));
            } else if (hovered) {
                g2.setColor(navIcon == NavIcon.LOGOUT ? DANGER_SOFT_BG : SURFACE_SUBTLE);
                g2.fill(new RoundRectangle2D.Double(2, 1, w - 4, h - 2, 10, 10));
            }

            Color iconCol = active ? PRIMARY_END : (navIcon == NavIcon.LOGOUT ? DANGER_END : TEXT_MUTED);
            paintNavIcon(g2, navIcon, 22, h / 2, iconCol);
            g2.dispose();
            super.paintComponent(g);
        }

        private void paintNavIcon(Graphics2D g2, NavIcon icon, int cx, int cy, Color color) {
            if (icon == null) return;
            Graphics2D g = (Graphics2D) g2.create();
            enableAntiAliasing(g);
            g.setColor(color);
            g.setStroke(new BasicStroke(1.65f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (icon) {
                case CALENDAR:
                    g.draw(new RoundRectangle2D.Double(cx - 7, cy - 6, 14, 12, 3, 3));
                    g.drawLine(cx - 7, cy - 2, cx + 7, cy - 2);
                    g.drawLine(cx - 3, cy - 8, cx - 3, cy - 5);
                    g.drawLine(cx + 3, cy - 8, cx + 3, cy - 5);
                    break;
                case LIST:
                    for (int dy = -4; dy <= 4; dy += 4) {
                        g.fill(new Ellipse2D.Double(cx - 7, cy + dy - 1, 2.2, 2.2));
                        g.drawLine(cx - 2, cy + dy, cx + 7, cy + dy);
                    }
                    break;
                case CROWN:
                    Path2D crown = new Path2D.Double();
                    crown.moveTo(cx - 7, cy + 5);
                    crown.lineTo(cx - 7, cy - 3);
                    crown.lineTo(cx - 3, cy + 1);
                    crown.lineTo(cx, cy - 5);
                    crown.lineTo(cx + 3, cy + 1);
                    crown.lineTo(cx + 7, cy - 3);
                    crown.lineTo(cx + 7, cy + 5);
                    crown.closePath();
                    g.draw(crown);
                    break;
                case CHART:
                    g.drawLine(cx - 6, cy + 6, cx - 6, cy + 1);
                    g.drawLine(cx, cy + 6, cx, cy - 5);
                    g.drawLine(cx + 6, cy + 6, cx + 6, cy - 2);
                    g.drawLine(cx - 8, cy + 6, cx + 8, cy + 6);
                    break;
                case COURT:
                    g.draw(new RoundRectangle2D.Double(cx - 7, cy - 5, 14, 10, 2, 2));
                    g.drawLine(cx, cy - 5, cx, cy + 5);
                    g.draw(new Ellipse2D.Double(cx - 2.5, cy - 2.5, 5, 5));
                    break;
                case LOGOUT:
                    g.draw(new Arc2D.Double(cx - 7, cy - 6, 10, 12, 60, 240, Arc2D.OPEN));
                    g.drawLine(cx - 1, cy, cx + 7, cy);
                    g.drawLine(cx + 4, cy - 3, cx + 7, cy);
                    g.drawLine(cx + 4, cy + 3, cx + 7, cy);
                    break;
            }
            g.dispose();
        }
    }

    /** Kartu ringkasan statistik KPI dengan label kecil, angka tebal, dan lencana aksen lembut. */
    static class KpiCard extends ShadowCardPanel {
        private final JLabel lblTitle;
        private final JLabel lblValue;
        private final JLabel lblSub;
        private final PillLabel lblBadge;

        KpiCard(String title, String value, String badgeText, Color badgeBg, Color badgeFg) {
            super(16, 7);
            setLayout(new BorderLayout(8, 6));
            setContentPadding(14, 16, 14, 16);

            lblTitle = new JLabel(title);
            lblTitle.setFont(fontLabel(12f));
            lblTitle.setForeground(TEXT_MUTED);

            lblBadge = new PillLabel(badgeText == null ? "" : badgeText, badgeBg, badgeFg, badgeFg, false);

            JPanel top = new JPanel(new BorderLayout(8, 0));
            top.setOpaque(false);
            top.add(lblTitle, BorderLayout.CENTER);
            if (badgeText != null && !badgeText.isEmpty()) top.add(lblBadge, BorderLayout.EAST);

            lblValue = new JLabel(value);
            lblValue.setFont(fontHeading(21f));
            lblValue.setForeground(TEXT_HEADING);

            lblSub = new JLabel(" ");
            lblSub.setFont(fontBody(11.5f));
            lblSub.setForeground(TEXT_MUTED);

            add(top, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
            add(lblSub, BorderLayout.SOUTH);
        }

        void setValue(String value) { lblValue.setText(value); }
        void setSubtitle(String subtitle) { lblSub.setText(subtitle == null || subtitle.isEmpty() ? " " : subtitle); }
    }

    /** Header tabel modern dengan latar lembut #F8FAFC dan garis bawah tipis. */
    static class ModernHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setFont(fontLabel(12f));
            setForeground(TEXT_MUTED);
            setBackground(SURFACE_SIDEBAR);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                    new EmptyBorder(0, 12, 0, 12)
            ));
            return this;
        }
    }

    /** Renderer sel teks tabel modern dengan padding lega dan efek hover baris. */
    static class ModernCellRenderer extends DefaultTableCellRenderer {
        private final int align;
        private final boolean bold;

        ModernCellRenderer(int align) {
            this(align, false);
        }

        ModernCellRenderer(int align, boolean bold) {
            this.align = align;
            this.bold = bold;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setHorizontalAlignment(align);
            setFont(bold ? fontLabel(13f) : fontBody(13f));
            setBorder(new EmptyBorder(0, 12, 0, 12));
            if (isSelected) {
                setBackground(TABLE_SELECT_BG);
                setForeground(TEXT_HEADING);
            } else if (row == getHoverRow(table)) {
                setBackground(TABLE_HOVER_BG);
                setForeground(TEXT_HEADING);
            } else {
                setBackground(SURFACE_CARD);
                setForeground(bold ? TEXT_HEADING : TEXT_BODY);
            }
            return this;
        }
    }

    /** Renderer tabel berbentuk kapsul pastel untuk kolom status dan jenis lapangan. */
    static class PillBadgeRenderer extends DefaultTableCellRenderer {
        private Color pillBg = BADGE_GRAY_BG;
        private Color pillAccent = BADGE_GRAY_ACCENT;
        private Color pillText = BADGE_GRAY_TEXT;
        private String badgeText = "";
        private boolean selectedRow;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", isSelected, false, row, column);
            selectedRow = isSelected;
            badgeText = value == null ? "" : value.toString();
            resolvePalette(value);
            setOpaque(true);
            if (isSelected) setBackground(TABLE_SELECT_BG);
            else if (row == getHoverRow(table)) setBackground(TABLE_HOVER_BG);
            else setBackground(SURFACE_CARD);
            return this;
        }

        private void resolvePalette(Object v) {
            if (v instanceof StatusBooking) {
                switch ((StatusBooking) v) {
                    case LUNAS:
                    case SELESAI:
                        pillBg = BADGE_GREEN_BG; pillAccent = BADGE_GREEN_ACCENT; pillText = BADGE_GREEN_TEXT;
                        return;
                    case DIBATALKAN:
                        pillBg = BADGE_RED_BG; pillAccent = BADGE_RED_ACCENT; pillText = BADGE_RED_TEXT;
                        return;
                    default:
                        pillBg = BADGE_AMBER_BG; pillAccent = BADGE_AMBER_ACCENT; pillText = BADGE_AMBER_TEXT;
                        return;
                }
            }
            if (v instanceof StatusUpgrade) {
                switch ((StatusUpgrade) v) {
                    case LUNAS:
                        pillBg = BADGE_GREEN_BG; pillAccent = BADGE_GREEN_ACCENT; pillText = BADGE_GREEN_TEXT;
                        return;
                    case DIBATALKAN:
                        pillBg = BADGE_RED_BG; pillAccent = BADGE_RED_ACCENT; pillText = BADGE_RED_TEXT;
                        return;
                    default:
                        pillBg = BADGE_AMBER_BG; pillAccent = BADGE_AMBER_ACCENT; pillText = BADGE_AMBER_TEXT;
                        return;
                }
            }
            if (v instanceof JenisLapangan) {
                switch ((JenisLapangan) v) {
                    case FUTSAL:
                        pillBg = BADGE_GREEN_BG; pillAccent = BADGE_GREEN_ACCENT; pillText = BADGE_GREEN_TEXT;
                        return;
                    case MINISOCCER:
                        pillBg = PRIMARY_SOFT_BG; pillAccent = PRIMARY_START; pillText = PRIMARY_PRESSED;
                        return;
                    case BASKET:
                        pillBg = BADGE_AMBER_BG; pillAccent = BADGE_AMBER_ACCENT; pillText = BADGE_AMBER_TEXT;
                        return;
                    case BADMINTON:
                        pillBg = BADGE_BLUE_BG; pillAccent = BADGE_BLUE_ACCENT; pillText = BADGE_BLUE_TEXT;
                        return;
                    case PADEL:
                    default:
                        pillBg = BADGE_RED_BG; pillAccent = BADGE_RED_ACCENT; pillText = BADGE_RED_TEXT;
                        return;
                }
            }
            pillBg = BADGE_BLUE_BG; pillAccent = BADGE_BLUE_ACCENT; pillText = BADGE_BLUE_TEXT;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (badgeText.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);
            g2.setFont(fontLabel(11.5f));
            FontMetrics fm = g2.getFontMetrics();

            int pillH = Math.min(getHeight() - 10, 24);
            int pillW = Math.min(getWidth() - 12, fm.stringWidth(badgeText) + 29);
            int x = 8;
            int y = (getHeight() - pillH) / 2;

            g2.setColor(pillBg);
            g2.fill(new RoundRectangle2D.Double(x, y, pillW, pillH, pillH, pillH));
            if (selectedRow) {
                g2.setColor(pillAccent);
                g2.draw(new RoundRectangle2D.Double(x, y, pillW, pillH, pillH, pillH));
            }
            g2.setColor(pillAccent);
            g2.fill(new Ellipse2D.Double(x + 8, y + (pillH - 6) / 2.0, 6, 6));

            g2.setColor(pillText);
            int textY = y + (pillH - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(badgeText, x + 18, textY);
            g2.dispose();
        }
    }

    /** Renderer sel jadwal pastel dengan garis aksen kiri 3px serta dukungan efek hover. */
    static class PastelSlotRenderer extends DefaultTableCellRenderer {
        private int hoverRow = -1;
        private int hoverCol = -1;
        private Color slotBg = BADGE_GRAY_BG;
        private Color slotAccent = BADGE_GRAY_ACCENT;
        private Color slotText = BADGE_GRAY_TEXT;
        private String labelText = "";
        private boolean highlighted;
        private boolean isTimeCol;

        void setHoveredCell(int row, int col) {
            this.hoverRow = row;
            this.hoverCol = col;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, "", false, false, row, column);
            labelText = value == null ? "" : value.toString();
            isTimeCol = (column == 0);
            highlighted = !isTimeCol && (isSelected || (row == hoverRow && column == hoverCol));

            if (!isTimeCol) {
                switch (labelText) {
                    case "Kosong":
                        slotBg = BADGE_GREEN_BG; slotAccent = BADGE_GREEN_ACCENT; slotText = BADGE_GREEN_TEXT;
                        break;
                    case "Terisi":
                        slotBg = BADGE_RED_BG; slotAccent = BADGE_RED_ACCENT; slotText = BADGE_RED_TEXT;
                        break;
                    case "Anda":
                        slotBg = BADGE_BLUE_BG; slotAccent = BADGE_BLUE_ACCENT; slotText = BADGE_BLUE_TEXT;
                        break;
                    default:
                        slotBg = BADGE_GRAY_BG; slotAccent = BADGE_GRAY_ACCENT; slotText = BADGE_GRAY_TEXT;
                        break;
                }
            }
            setOpaque(true);
            setBackground(isTimeCol ? SURFACE_SIDEBAR : SURFACE_CARD);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);

            if (isTimeCol) {
                g2.setFont(fontLabel(12.5f));
                g2.setColor(TEXT_BODY);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(labelText)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(labelText, tx, ty);
                g2.dispose();
                return;
            }

            int mx = 5;
            int my = 4;
            int w = getWidth() - mx * 2;
            int h = getHeight() - my * 2;
            if (w > 6 && h > 6) {
                Color fill = highlighted && "Kosong".equals(labelText)
                        ? new Color(0xBB, 0xF7, 0xD0)
                        : (highlighted ? slotBg.darker() : slotBg);
                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Double(mx, my, w, h, 8, 8));

                // Garis aksen vertikal 3px di sisi kiri
                g2.setColor(slotAccent);
                g2.fill(new RoundRectangle2D.Double(mx, my, 3.5, h, 3, 3));

                g2.setFont(fontLabel(12f));
                g2.setColor(slotText);
                FontMetrics fm = g2.getFontMetrics();
                String display = "-".equals(labelText) ? "Tidak Tersedia" : labelText;
                int tx = mx + (w - fm.stringWidth(display)) / 2 + 1;
                int ty = my + (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(display, tx, ty);
            }
            g2.dispose();
        }
    }

    /** Scrollbar ramping membulat tanpa tombol panah klasik Swing. */
    static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = BORDER_INPUT;
            thumbHighlightColor = SCROLL_THUMB_HOVER;
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }

        private JButton zeroButton() {
            JButton b = new JButton();
            Dimension zero = new Dimension(0, 0);
            b.setPreferredSize(zero);
            b.setMinimumSize(zero);
            b.setMaximumSize(zero);
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            if (c.isOpaque()) {
                g.setColor(SURFACE_CARD);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntiAliasing(g2);
            g2.setColor(isDragging || isThumbRollover() ? SCROLL_THUMB_HOVER : BORDER_INPUT);
            int pad = 2;
            int w = Math.max(4, thumbBounds.width - pad * 2);
            int h = Math.max(4, thumbBounds.height - pad * 2);
            int arc = Math.min(w, h);
            g2.fill(new RoundRectangle2D.Double(thumbBounds.x + pad, thumbBounds.y + pad, w, h, arc, arc));
            g2.dispose();
        }
    }

    /** Menerapkan ModernScrollBarUI dan border bersih ke JScrollPane. */
    static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        sp.setOpaque(false);
        sp.setBackground(SURFACE_CARD);
        sp.getViewport().setOpaque(true);
        sp.getViewport().setBackground(SURFACE_CARD);

        JScrollBar vBar = sp.getVerticalScrollBar();
        if (vBar != null) {
            vBar.setUI(new ModernScrollBarUI());
            vBar.setOpaque(false);
            vBar.setBackground(SURFACE_CARD);
            vBar.setPreferredSize(new Dimension(10, 0));
            vBar.setUnitIncrement(16);
        }
        JScrollBar hBar = sp.getHorizontalScrollBar();
        if (hBar != null) {
            hBar.setUI(new ModernScrollBarUI());
            hBar.setOpaque(false);
            hBar.setBackground(SURFACE_CARD);
            hBar.setPreferredSize(new Dimension(0, 10));
            hBar.setUnitIncrement(16);
        }

        JPanel corner = new JPanel();
        corner.setBackground(SURFACE_CARD);
        sp.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, corner);
    }
}

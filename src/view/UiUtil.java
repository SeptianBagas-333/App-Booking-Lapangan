package view;

import model.*;
import util.Fmt;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/** Helper tampilan yang dipakai bersama oleh semua frame. */
final class UiUtil {
    static final Color HIJAU = new Color(198, 239, 206);
    static final Color MERAH = new Color(255, 199, 206);
    static final Color BIRU = new Color(189, 215, 238);
    static final Color ABU = new Color(224, 224, 224);

    private UiUtil() {}

    static DefaultTableModel modelTabel() {
        return new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static JTable tabel(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(26);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    static void error(Component p, String pesan) {
        JOptionPane.showMessageDialog(p, pesan, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }

    static void info(Component p, String pesan) {
        JOptionPane.showMessageDialog(p, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean konfirmasi(Component p, String pesan) {
        return JOptionPane.showConfirmDialog(p, pesan, "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
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
        int kolStatus = admin ? 7 : 6;
        tabel.getColumnModel().getColumn(kolStatus + 1).setPreferredWidth(300);
        tabel.getColumnModel().getColumn(kolStatus).setCellRenderer(new StatusRenderer());
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
        int kolStatus = admin ? 6 : 5;
        tabel.getColumnModel().getColumn(kolStatus).setCellRenderer(new StatusRenderer());
        tabel.getColumnModel().getColumn(kolStatus + 1).setPreferredWidth(130);
        tabel.getColumnModel().getColumn(kolStatus + 2).setPreferredWidth(240);
    }

    static String idTerpilih(JTable t) {
        int r = t.getSelectedRow();
        return r < 0 ? null : (String) t.getModel().getValueAt(t.convertRowIndexToModel(r), 0);
    }

    /** Memberi warna pada kolom status. */
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            if (!sel && v instanceof StatusBooking) {
                switch ((StatusBooking) v) {
                    case LUNAS: comp.setBackground(HIJAU); break;
                    case SELESAI: comp.setBackground(BIRU); break;
                    case DIBATALKAN: comp.setBackground(MERAH); break;
                    default: comp.setBackground(new Color(255, 235, 156));
                }
                comp.setForeground(Color.BLACK);
            } else if (!sel && v instanceof StatusUpgrade) {
                switch ((StatusUpgrade) v) {
                    case LUNAS: comp.setBackground(HIJAU); break;
                    case DIBATALKAN: comp.setBackground(MERAH); break;
                    default: comp.setBackground(new Color(255, 235, 156));
                }
                comp.setForeground(Color.BLACK);
            } else if (!sel) {
                comp.setBackground(Color.WHITE);
            }
            return comp;
        }
    }

    static JPanel panelTombol(JButton... tombol) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        for (JButton b : tombol) p.add(b);
        return p;
    }
}

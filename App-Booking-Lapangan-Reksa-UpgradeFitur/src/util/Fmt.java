package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Helper format tampilan (rupiah, tanggal, jam). */
public final class Fmt {
    public static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter JAM = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter LENGKAP = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private Fmt() {}

    public static String rp(double nilai) {
        return "Rp" + String.format(Locale.US, "%,.0f", nilai).replace(',', '.');
    }

    public static String waktu(LocalDateTime t) { return t.format(LENGKAP); }
}

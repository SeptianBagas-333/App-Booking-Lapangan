package repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Utilitas baca/tulis file teks dengan pemisah kolom '|'. */
final class CsvFile {
    private CsvFile() {}

    static List<String[]> baca(Path file) {
        List<String[]> hasil = new ArrayList<>();
        try {
            for (String baris : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!baris.isBlank()) hasil.add(baris.split("\\|", -1));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca " + file, e);
        }
        return hasil;
    }

    static void tulis(Path file, List<String> baris) {
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, baris, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menulis " + file, e);
        }
    }
}

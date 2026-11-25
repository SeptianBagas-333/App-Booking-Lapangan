package model;

import java.time.LocalDateTime;

public interface Cancelable {
    boolean bolehDibatalkan(LocalDateTime sekarang);
    double hitungRefund(LocalDateTime sekarang);
}

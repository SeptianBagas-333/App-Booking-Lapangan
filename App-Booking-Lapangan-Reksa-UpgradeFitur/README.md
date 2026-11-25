# Sistem Booking Lapangan Olahraga (Java Swing)

Jalankan: `./run.sh` (Linux/Mac) atau `run.bat` (Windows). Butuh JDK 17 ke atas.
Tes logika & GUI smoke: `javac -encoding UTF-8 -d out $(find src test -name '*.java') && java -ea -cp out LogicTest && java -cp out GuiSmoke`

## Akun demo
| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Pelanggan (Gold 10%) | budi | budi123 |
| Pelanggan (biasa) | siti | siti123 |

Data tersimpan di folder `data/` (CSV, dibuat otomatis saat pertama jalan). Hapus folder itu untuk reset.
Struk tersimpan di folder `struk/`.

## Struktur
- `model` : User (abstract) > Admin/Pelanggan; Lapangan (abstract) > 5 jenis; Booking; UpgradeMember; interface Pembayaran, Diskon, Cancelable; enum StatusBooking, StatusUpgrade, JenisLapangan, Member
- `repository` : baca/tulis CSV (`users.csv`, `lapangan.csv`, `booking.csv`, `upgrade_member.csv`)
- `service` : AuthService, BookingService, UpgradeMemberService (seluruh logika bisnis)
- `view` : LoginFrame, PelangganFrame, AdminFrame, UiUtil (Java Swing soft-modern UI, tema terang lintas OS, jendela utama otomatis maximized)
- `exception` : 4 custom exception

## Aturan bisnis
- Jam buka 08.00-22.00, durasi 1-3 jam, booking minimal 60 menit sebelum main
- Jam sibuk 17.00-22.00 x1,25; Sabtu-Minggu x1,15; biaya tambahan per jenis lapangan
- Diskon member: Silver 5%, Gold 10%
- Batas bayar 30 menit (otomatis batal tanpa refund)
- E-Wallet langsung LUNAS; Transfer/Tunai menunggu konfirmasi admin
- Refund (hanya booking LUNAS): >=24 jam 100%, 3-24 jam 50%, <3 jam 0%
- Upgrade member (hanya ke tier lebih tinggi, tidak boleh ganda saat masih menunggu konfirmasi): BIASA -> SILVER Rp50.000, BIASA -> GOLD Rp75.000, SILVER -> GOLD Rp30.000
- Pembayaran upgrade: E-Wallet langsung LUNAS & tier langsung aktif; Transfer/Tunai menunggu konfirmasi admin
- Koreksi admin (Batalkan & Downgrade): transaksi upgrade LUNAS terbaru milik pelanggan dapat dibatalkan, tier kembali ke tier asal, dan nominal dihapus dari laporan pendapatan
- Laporan pendapatan admin menggabungkan pendapatan bersih booking (setelah refund) dan pendapatan upgrade member berstatus LUNAS

# Chess Native App

Aplikasi catur Android native (Kotlin, tanpa WebView, tanpa internet).

## Kenapa dibuat ulang dari versi WebView?

Versi sebelumnya membuka chess.com penuh lewat WebView, sehingga
menjalankan seluruh engine Chromium + JS + aset chess.com hanya untuk
menampilkan papan catur. Ini membuat pemakaian RAM jauh lebih besar
(~226 MB) dibanding aplikasi lain di perangkat.

Versi ini menggantinya dengan:

- **Custom `View`** yang menggambar papan & bidak langsung lewat `Canvas`
  (simbol Unicode catur, bukan gambar/bitmap, sehingga tidak butuh aset).
- **Chess engine murni Kotlin** (`ChessEngine.kt`) — validasi langkah per
  jenis bidak, deteksi skak, dan cegah langkah yang membuat raja sendiri
  skak.
- **Tanpa WebView, tanpa izin internet** — semua logika berjalan lokal
  di device.

Estimasi pemakaian RAM: sekitar 10–30 MB, jauh di bawah versi WebView.

## Struktur

```
app/src/main/java/com/zhallrizqi2/chess/
├── MainActivity.kt          # entry point, hubungkan UI
├── ChessBoardView.kt        # gambar papan + tangani sentuhan
└── model/
    ├── ChessEngine.kt       # aturan & validasi langkah catur
    └── Piece.kt             # data class bidak & kotak
```

## Fitur

- Gerak legal semua bidak (pion, kuda, gajah, benteng, ratu, raja)
- Giliran otomatis putih/hitam
- Deteksi skak & skakmat
- Promosi pion otomatis jadi ratu
- Tombol "Game Baru"

## Belum didukung (disederhanakan agar tetap ringan & mudah dibaca)

- Castling (rokade)
- En passant
- Deteksi remis (repetisi 3x, 50-langkah, dsb.)
- Lawan AI (saat ini 2 pemain bergantian di 1 layar)

## Cara pakai

1. Buka folder ini di Android Studio.
2. Sync Gradle, lalu Run ke device/emulator.
3. Ganti isi file lama di repo `chess-webview-app` kamu dengan struktur
   ini, atau buat repo baru.

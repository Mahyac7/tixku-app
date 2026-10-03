# Tixku 🎬🎟️

Aplikasi Android pemesanan tiket film & event, dibangun mengikuti **system design BookMyShow**. Dibuat dengan **Kotlin + Jetpack Compose** (Material 3) dan arsitektur MVVM/Repository.

> **Catatan:** Ini adalah **frontend Android** yang mensimulasikan backend berskala besar dari system design. Service-service seperti Search, Movie, Theatre, Show, Booking, dan Payment direpresentasikan oleh satu `TixkuRepository` in-memory, sehingga mudah disambungkan ke backend sungguhan nanti.

## Fitur (mengikuti Functional Requirements)

- 📍 **Pilih kota** sebelum menjelajah
- 🔍 **Cari film** berdasarkan judul, genre, atau bahasa (Search Service)
- 🎞️ **Detail film**: sinopsis, pemain, rating, durasi, format
- 🏢 **Jadwal tayang** per bioskop dengan format 2D/3D/IMAX dan harga
- 💺 **Pemilihan kursi real-time** dengan status: Tersedia / Pilihanmu / Dikunci / Terisi
- 🔒 **Seat locking + consistency** — kursi dikunci secara atomik sebelum pembayaran, dengan TTL 5 menit (mensimulasikan Redis). Hanya satu user yang bisa memesan satu kursi (mencegah *double booking*).
- 💳 **Simulasi pembayaran** (Payment Gateway) dengan sukses/gagal
- 🎫 **E-ticket dengan QR code** (dibuat dengan ZXing)
- 📜 **Riwayat booking** & pembatalan

## Pemetaan ke System Design

| System Design | Implementasi di Tixku |
|---|---|
| Movie / Theatre / Show Service | `TixkuRepository` + `MockData` |
| Search Service (Elasticsearch) | `TixkuRepository.searchMovies()` |
| Booking Service + seat lock (Redis TTL) | `lockSeats()` / `releaseSeats()` dengan expiry |
| Payment Service | `confirmBooking(paymentSuccess)` |
| Primary Database (PostgreSQL/MySQL) | `MockData` (data in-memory) |
| Object Storage + CDN (poster) | Placeholder gradient `MoviePoster` |
| Notification (e-ticket QR) | `generateQrBitmap()` |

## User Journey

Pilih Kota → Cari Film → Detail & Jadwal → Pilih Kursi → Kunci & Bayar → E-Ticket (QR) → Kelola Booking

## Build

```bash
./gradlew :app:assembleDebug
```

APK dihasilkan di `app/build/outputs/apk/debug/app-debug.apk`.

### Prasyarat
- JDK 17
- Android SDK (compileSdk 34, build-tools 34.0.0)
- minSdk 24, targetSdk 34

## Struktur

```
app/src/main/java/com/tixku/app/
├── MainActivity.kt            # Navigasi (Compose Navigation)
├── data/
│   ├── Models.kt              # Entitas: Movie, Cinema, Show, Seat, Booking...
│   ├── MockData.kt            # Seed data (pengganti Primary DB)
│   └── TixkuRepository.kt     # Logika service + seat locking
├── ui/
│   ├── theme/Theme.kt
│   ├── components/MoviePoster.kt
│   └── screens/               # City, Home, MovieDetail, SeatSelection, Booking...
└── util/Util.kt               # Format rupiah + QR generator
```

## Roadmap (menuju arsitektur penuh)
- Sambungkan repository ke REST/GraphQL API Gateway sungguhan
- Autentikasi (OAuth/JWT) via Auth Service
- Caching lokal dengan Room, offline support
- Integrasi payment gateway asli (Midtrans/Stripe)

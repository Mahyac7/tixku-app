# Tixku REST API

Kontrak REST untuk backend Tixku. Spesifikasi OpenAPI lengkap ada di [`openapi.yaml`](./openapi.yaml).

- **Base URL (prod):** `https://api.tixku.app/v1`
- **Base URL (lokal, emulator Android):** `http://10.0.2.2:8080/v1`
  (`10.0.2.2` adalah alias ke `localhost` mesin host dari dalam emulator.)
- **Auth:** `Authorization: Bearer <JWT>` untuk endpoint yang membutuhkan login (Bookings & Payments).
- **Format:** JSON. Semua waktu dalam ISO-8601 UTC.

## Pemetaan endpoint → microservice (system design)

| Service | Endpoint |
|---|---|
| Auth Service | `POST /auth/login`, `POST /auth/register` |
| Search Service | `GET /search/movies` |
| Movie Service | `GET /movies/{id}` |
| Theatre + Show Service | `GET /movies/{id}/shows`, `GET /shows/{id}`, `GET /shows/{id}/seats` |
| Booking Service (seat lock) | `POST /shows/{id}/locks`, `DELETE /shows/{id}/locks/{lockId}`, `POST /bookings`, `POST /bookings/{id}/cancel` |
| Payment Service | `POST /payments`, `GET /payments/{id}` |

## Alur pemesanan (seat locking + consistency)

```
1. GET  /shows/{id}/seats           → tampilkan peta kursi
2. POST /shows/{id}/locks           → kunci kursi (atomik, all-or-nothing)
     ├─ 201 → { lockId, expiresAt }  (lanjut)
     └─ 409 → SEAT_TAKEN             (minta user pilih ulang)
3. POST /payments                   → { paymentId, status: PENDING }
4. GET  /payments/{paymentId}       → poll sampai SUCCESS / FAILED
5. POST /bookings                   → { lockId, paymentId } → booking + QR
     ├─ 201 → Booking (CONFIRMED)
     ├─ 402 → pembayaran belum sukses
     └─ 409 → lock kedaluwarsa (TTL habis)
```

Kunci (lock) memiliki **TTL** (mis. 5 menit) dan otomatis kedaluwarsa di cache
(Redis), sehingga kursi tidak "nyangkut" bila user tidak menyelesaikan
pembayaran. Operasi lock bersifat **all-or-nothing**: hanya satu sesi yang bisa
memegang kursi pada satu waktu — inilah penyelesaian tantangan *double booking*.

## Contoh

### Cari film
```http
GET /v1/search/movies?cityId=jkt&q=action
```
```json
{
  "content": [
    { "id": "m2", "title": "Protocol Zero", "genres": ["Action","Thriller"],
      "language": "English", "durationMinutes": 134, "rating": 7.9,
      "synopsis": "...", "cast": ["Chris Dalton"], "posterColor": "FF3949AB",
      "certification": "D17+" }
  ],
  "page": 0, "size": 20, "totalElements": 1
}
```

### Kunci kursi
```http
POST /v1/shows/s1/locks
Authorization: Bearer <JWT>
Content-Type: application/json

{ "seatIds": ["A1", "A2"] }
```
```json
{ "lockId": "lock_8f3a", "showId": "s1", "seatIds": ["A1","A2"],
  "expiresAt": "2026-10-03T16:05:00Z" }
```
Jika gagal:
```json
{ "code": "SEAT_TAKEN", "message": "Salah satu kursi baru saja diambil orang lain." }
```

### Buat pembayaran lalu konfirmasi booking
```http
POST /v1/payments
{ "lockId": "lock_8f3a", "amount": 120000, "method": "QRIS" }
→ { "id": "pay_123", "status": "PENDING", "amount": 120000 }

POST /v1/bookings
{ "lockId": "lock_8f3a", "paymentId": "pay_123" }
→ { "id": "TIX-A1B2C3D4", "status": "CONFIRMED", "seatIds": ["A1","A2"],
    "qrPayload": "TIXKU|TIX-A1B2C3D4|...", ... }
```

## Catatan integrasi Android

Aplikasi menggunakan **Retrofit + OkHttp + Moshi**. Base URL dikonfigurasi di
`BuildConfig.API_BASE_URL` (lihat `app/build.gradle.kts`). Flag
`BuildConfig.USE_REMOTE_API` menentukan apakah app memakai backend REST atau
data lokal (mock) — berguna saat backend belum tersedia.

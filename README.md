# ifs25045-pbo2026-springboot-cashflow

Aplikasi web pencatat transaksi keuangan pribadi (Cashflow) berbasis **Spring Boot**, **Maven**, dan **Vaadin**, dengan arsitektur **Clean Architecture**.

Setiap transaksi bertipe **Pemasukan** (`INCOME`) atau **Pengeluaran** (`EXPENSE`). Aplikasi menampilkan daftar transaksi dan menghitung saldo (total pemasukan − total pengeluaran).
Data disimpan **in-memory** (belum memakai database). Port dikonfigurasi lewat `.env` (`APP_PORT`).

> **Penamaan proyek mahasiswa:** `{username}-pbo2026-springboot-cashflow`  
> **Package:** `org.delcom.{username}pbo2026springbootcashflow`  
> Contoh di repo ini memakai username `ifs25045`. Ganti sesuai username Anda.

## Prasyarat

- JDK 25
- Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Struktur Layer

```text
ifs25045-pbo2026-springboot-cashflow/
├── .env / .env.example
├── pom.xml
├── src/main/java/org/delcom/ifs25045pbo2026springbootcashflow/
│   ├── App.java              # entry point (@SpringBootApplication)
│   ├── DotEnv.java           # muat .env ke system properties
│   ├── config/AppConfig.java # composition root Spring (@Bean)
│   ├── domain/
│   │   ├── entity/           # Transaction, TransactionType, SortOption
│   │   ├── exception/        # DomainException, EntityNotFoundException, ValidationException
│   │   └── repository/       # IRepository<T, ID>, ITransactionRepository
│   ├── usecase/              # FinanceUseCase (termasuk getBalance)
│   ├── adapter/
│   │   ├── repository/       # InMemoryRepository<T, ID>, TransactionRepository
│   │   └── presenter/        # FinancePresenter
│   └── framework/view/       # FinanceView (Vaadin)
├── src/main/resources/
│   └── application.yaml
└── src/test/java/.../        # AppTest + unit test per layer
```

Dependency Rule: `domain ← usecase ← adapter / framework`. Use case tidak memuat komponen Vaadin, presenter terpisah dari entity, dan seluruh wiring ada di `AppConfig`.

## Konfigurasi port (`.env`)

```env
APP_PORT=8080
```

`application.yaml` memakai:

```yaml
server:
  port: ${APP_PORT:8080}
```

Salin dari `.env.example` jika `.env` belum ada. File `.env` diabaikan git.

## Perintah Build & Menjalankan

### 1. Development (disarankan)

```powershell
.\mvnw.cmd spring-boot:run
```

Buka [http://localhost:8080](http://localhost:8080) (atau port di `APP_PORT`).

Spring Boot DevTools aktif: simpan file → compile → auto-restart + live-reload.
Kalau autobuild IDE tidak jalan:

```powershell
.\dev.ps1
```

### 2. Uji + coverage Jacoco 100%

```powershell
.\mvnw.cmd clean verify
```

Laporan: `target/site/jacoco/index.html`

### 3. Build & jalankan JAR (wajib `-Pproduction`)

Tanpa `-Pproduction`, `java -jar` gagal (`vaadin-dev-server not found`) karena `vaadin-dev` tidak ikut ke JAR.

```powershell
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25045-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

Jalankan dari root proyek agar file `.env` terbaca.

## Fitur UI

- Kartu ringkasan: total transaksi, total pemasukan, total pengeluaran, dan saldo
- Tambah pemasukan dan tambah pengeluaran lewat dialog (keterangan + jumlah)
- Cari berdasarkan keterangan (case-insensitive; hanya memfilter, tidak mengubah data)
- Urutkan lewat klik header kolom (jumlah dan tipe memakai comparator `SortOption` di domain)
- Hapus transaksi dengan dialog konfirmasi
- Validasi: keterangan kosong, jumlah non-positif / non-numerik, dan ID tidak valid / tidak ditemukan
  ditampilkan sebagai notifikasi error
- Jumlah selalu berformat pemisah ribuan + 2 desimal, contoh `Rp 1,000,000.00`
- Penyimpanan sementara di memori (reset saat aplikasi di-restart)

## Optimasi Web (Lighthouse, Axe, SEO)

Perubahan di `framework/web` dan `FinanceView` menjawab temuan Web Grading:

| Temuan | Akar masalah | Perbaikan |
|---|---|---|
| Kontras warna (Axe) | Label kartu memakai warna aksen terang; tombol "success" Aura `#00a045` | Semua teks/latar ≥ 4,5:1 (kartu, header dialog, tombol) |
| Landmark `main` | Konten tidak berada dalam landmark | `FinanceView` ber-`role="main"`; ringkasan `region`, toolbar `group`, status `role="status"` |
| `lang="en"` | Vaadin mengisi `<html lang>` dari locale server | `IndonesianI18NProvider` → `lang="id"` |
| Meta description, title | Tidak ada AppShell | `CashflowAppShell` (`@Meta`, judul, favicon) |
| `robots.txt` tidak valid | URL tidak ada, Vaadin membalas HTML | File statis `META-INF/resources/robots.txt` |
| Error konsol | `/favicon.ico` 404 | Favicon SVG + `<link rel="icon">` |
| Cache efisien | Aset ber-hash hanya di-cache 1 jam | `HttpCachePolicyFilter`: `/VAADIN/build/**` immutable 1 tahun, tema 30 hari, halaman/XHR `no-store` |
| Transfer besar | Respons tanpa kompresi | `server.compression` di `application.yaml` |
| JS/main-thread | Iconset `@vaadin/icons`, kolom `autoWidth`, `executeJs` pada dialog | Tanpa `VaadinIcon`, lebar kolom tetap, `DialogVariant.NO_PADDING` |

> **Penting:** tanpa `AppShellConfigurator`, Vaadin 25 memuat tema Aura otomatis. Karena `CashflowAppShell`
> sekarang ada, tema dinyatakan eksplisit (`@StyleSheet(Aura.STYLESHEET)`). Jangan hapus anotasi itu.

`robots.txt` mengizinkan crawling karena audit SEO Lighthouse menggagalkan halaman yang diblokir/`noindex`.
Aplikasi ini belum punya autentikasi dan datanya hanya di memori; bila nanti menyimpan data pribadi,
tambahkan autentikasi dan ganti dengan `Disallow: /` serta `X-Robots-Tag: noindex`.

Verifikasi: `./mvnw clean verify -Pproduction` lalu jalankan JAR dan audit URL deployment dengan Lighthouse/Axe.

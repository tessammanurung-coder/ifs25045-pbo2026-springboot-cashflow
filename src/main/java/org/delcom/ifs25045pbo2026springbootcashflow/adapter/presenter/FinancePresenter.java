package org.delcom.ifs25045pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.TransactionType;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Memisahkan logika tampilan dari entity, use case, dan view sehingga
 * format output bisa diubah tanpa menyentuh domain.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Memformat jumlah uang dengan pemisah ribuan (koma) dan dua desimal,
     * contoh: 1000000 menjadi "Rp 1,000,000.00". Nilai negatif menjadi "-Rp 500.00".
     */
    public String formatAmount(double amount) {
        String number = String.format(Locale.US, "%,.2f", Math.abs(amount));
        return (amount < 0 ? "-Rp " : "Rp ") + number;
    }

    /** Memformat jenis transaksi: "Pemasukan" atau "Pengeluaran". */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Memformat satu transaksi menjadi baris teks untuk ditampilkan. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatAmount(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Memformat waktu transaksi dicatat. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Memformat saldo, contoh: "Saldo: Rp 4,975,000.00". */
    public String formatBalance(double balance) {
        return "Saldo: " + formatAmount(balance);
    }

    /**
     * Memformat daftar transaksi menjadi teks multi-baris.
     * Menambahkan pesan kosong jika list tidak berisi data.
     */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            builder.append('\n').append(emptyMessage);
            return builder.toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        builder.append('\n').append(body);
        return builder.toString();
    }

    /** Memformat daftar semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", "- Data transaksi belum tersedia!");
    }

    /** Memformat hasil pencarian berdasarkan kata kunci. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", "- Transaksi tidak ditemukan!");
    }

    /** Memformat daftar transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", "- Data transaksi belum tersedia!");
    }

    /** Pesan sukses setelah menambah transaksi. */
    public String showAddSuccess(Transaction transaction) {
        return "Berhasil menambah transaksi: " + format(transaction);
    }

    /** Pesan sukses setelah menghapus transaksi. */
    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    /** Pesan gagal saat menghapus transaksi. */
    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    /** Pesan saat pilihan/aksi tidak dikenali. */
    public String showInvalidChoice() {
        return "[!] Pilihan tidak dimengerti.";
    }

    /** Pesan saat ID yang dimasukkan tidak valid. */
    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    /** Pesan validasi umum dari domain. */
    public String showValidationError(String message) {
        return "[!] " + message;
    }
}

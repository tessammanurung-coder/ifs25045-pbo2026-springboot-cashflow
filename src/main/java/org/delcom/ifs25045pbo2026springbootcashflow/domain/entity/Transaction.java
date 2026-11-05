package org.delcom.ifs25045pbo2026springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan.
 * Berada di layer domain — tidak bergantung pada layer lain dan bebas dari
 * urusan tampilan (format uang) maupun penyimpanan.
 */
public class Transaction {
    /** ID unik transaksi, tidak boleh diubah setelah dibuat. */
    private final int id;

    /** Keterangan transaksi. */
    private String description;

    /** Jumlah uang; selalu positif, arah (+/-) ditentukan oleh {@link #type}. */
    private double amount;

    /** Jenis transaksi: pemasukan atau pengeluaran. */
    private final TransactionType type;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    /** Membuat transaksi baru yang dicatat pada waktu sekarang. */
    public Transaction(int id, String description, double amount, TransactionType type) {
        this(id, description, amount, type, LocalDateTime.now());
    }

    /** Membuat transaksi dengan waktu pencatatan yang sudah ditentukan. */
    public Transaction(int id, String description, double amount, TransactionType type, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Dampak transaksi terhadap saldo: positif untuk pemasukan,
     * negatif untuk pengeluaran.
     */
    public double signedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }

    /** Mengubah keterangan transaksi. */
    public void changeDescription(String description) {
        this.description = description;
    }

    /** Mengubah jumlah transaksi. */
    public void changeAmount(double amount) {
        this.amount = amount;
    }
}

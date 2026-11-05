package org.delcom.ifs25045pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan: pemasukan menambah saldo, pengeluaran mengurangi saldo.
 * Urutan deklarasi dipakai sebagai urutan alami (INCOME lebih dulu dari EXPENSE).
 */
public enum TransactionType {
    /** Pemasukan (menambah saldo). */
    INCOME,

    /** Pengeluaran (mengurangi saldo). */
    EXPENSE
}

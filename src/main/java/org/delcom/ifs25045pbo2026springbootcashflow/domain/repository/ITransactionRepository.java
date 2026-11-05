package org.delcom.ifs25045pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;

/**
 * Port spesifik Transaction yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan transaksi baru dengan penomoran ID otomatis,
 * dipisah untuk pemasukan dan pengeluaran.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /**
     * Menyimpan transaksi pemasukan baru.
     *
     * @param description keterangan transaksi
     * @param amount      jumlah uang (positif)
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveIncome(String description, double amount);

    /**
     * Menyimpan transaksi pengeluaran baru.
     *
     * @param description keterangan transaksi
     * @param amount      jumlah uang (positif)
     * @return transaksi yang tersimpan (lengkap dengan ID)
     */
    Transaction saveExpense(String description, double amount);
}

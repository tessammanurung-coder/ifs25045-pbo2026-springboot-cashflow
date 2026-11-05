package org.delcom.ifs25045pbo2026springbootcashflow.usecase;

import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Objects;

/**
 * Use case yang menangani logika bisnis aplikasi cashflow.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {
    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi yang tersedia. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambahkan transaksi pemasukan setelah validasi. */
    public Transaction addIncome(String description, Double amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveIncome(description.trim(), amount);
    }

    /** Menambahkan transaksi pengeluaran setelah validasi. */
    public Transaction addExpense(String description, Double amount) throws ValidationException {
        validate(description, amount);
        return transactionRepository.saveExpense(description.trim(), amount);
    }

    /**
     * Menghapus transaksi berdasarkan ID.
     * Melempar {@link ValidationException} jika ID tidak valid (bukan bilangan positif)
     * dan {@link EntityNotFoundException} jika tidak ditemukan.
     */
    public void removeTransaction(int id) throws EntityNotFoundException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
        boolean deleted = transactionRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String query = Objects.requireNonNullElse(keyword, "").trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(query));
    }

    /** Mengurutkan transaksi sesuai kriteria {@link SortOption} yang dipilih. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public double getTotalIncome() {
        return sumByType(TransactionType.INCOME);
    }

    /** Total seluruh pengeluaran. */
    public double getTotalExpense() {
        return sumByType(TransactionType.EXPENSE);
    }

    /** Saldo saat ini: total pemasukan dikurangi total pengeluaran. */
    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    private double sumByType(TransactionType type) {
        return transactionRepository.findBy(transaction -> transaction.getType() == type).stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void validate(String description, Double amount) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new ValidationException("Jumlah transaksi harus berupa angka lebih dari 0");
        }
    }
}

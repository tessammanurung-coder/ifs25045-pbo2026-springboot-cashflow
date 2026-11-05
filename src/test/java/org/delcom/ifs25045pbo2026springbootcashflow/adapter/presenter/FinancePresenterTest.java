package org.delcom.ifs25045pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancePresenterTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2026, 11, 1, 8, 0);

    private FinancePresenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new FinancePresenter();
    }

    @Test
    void formatAmountUsesThousandSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(1_000_000));
        assertEquals("Rp 1,234.50", presenter.formatAmount(1234.5));
        assertEquals("Rp 0.00", presenter.formatAmount(0));
        assertEquals("-Rp 500.00", presenter.formatAmount(-500));
    }

    @Test
    void formatTypeAndFormatTransaction() {
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.formatType(TransactionType.EXPENSE));

        Transaction income = new Transaction(1, "Gaji", 1_000_000, TransactionType.INCOME, CREATED);
        Transaction expense = new Transaction(2, "Makan", 25_000, TransactionType.EXPENSE, CREATED);
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
    }

    @Test
    void formatCreatedAtAndBalance() {
        Transaction transaction = new Transaction(1, "Gaji", 1, TransactionType.INCOME, CREATED);
        assertEquals("01/11/2026 08:00", presenter.formatCreatedAt(transaction));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
        assertEquals("Saldo: -Rp 10.00", presenter.formatBalance(-10));
    }

    @Test
    void formatListEmptyAndFilled() {
        assertEquals("Header:\n- kosong", presenter.formatList(List.of(), "Header:", "- kosong"));

        Transaction transaction = new Transaction(1, "Gaji", 1_000, TransactionType.INCOME, CREATED);
        String filled = presenter.formatList(List.of(transaction), "Header:", "- kosong");
        assertEquals("Header:\n[1] Gaji | Rp 1,000.00 | [Pemasukan]", filled);
    }

    @Test
    void showMessages() {
        Transaction transaction = new Transaction(1, "Gaji", 1_000, TransactionType.INCOME, CREATED);

        assertTrue(presenter.showTransactions(List.of()).contains("belum tersedia"));
        assertTrue(presenter.showTransactions(List.of(transaction)).contains("Gaji"));
        assertTrue(presenter.showSearchResults(List.of(), "x").contains("tidak ditemukan"));
        assertTrue(presenter.showSearchResults(List.of(transaction), "Gaji").contains("Hasil Pencarian: \"Gaji\""));
        assertTrue(presenter.showSortedTransactions(List.of(transaction)).contains("Terurut"));
        assertEquals("Berhasil menambah transaksi: [1] Gaji | Rp 1,000.00 | [Pemasukan]",
                presenter.showAddSuccess(transaction));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 9.", presenter.showRemoveFailed(9));
        assertEquals("[!] Pilihan tidak dimengerti.", presenter.showInvalidChoice());
        assertEquals("[!] ID tidak valid!", presenter.showInvalidId());
        assertEquals("[!] Keterangan transaksi tidak boleh kosong",
                presenter.showValidationError("Keterangan transaksi tidak boleh kosong"));
    }
}

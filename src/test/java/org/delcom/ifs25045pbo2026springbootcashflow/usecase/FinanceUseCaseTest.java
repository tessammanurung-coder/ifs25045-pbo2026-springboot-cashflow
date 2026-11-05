package org.delcom.ifs25045pbo2026springbootcashflow.usecase;

import org.delcom.ifs25045pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void addIncomeAndExpenseTrimDescriptionAndAssignIds() throws Exception {
        Transaction income = useCase.addIncome("  Gaji  ", 5_000_000.0);
        Transaction expense = useCase.addExpense("Makan", 25_000.0);

        assertEquals(1, income.getId());
        assertEquals("Gaji", income.getDescription());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void addRejectsBlankOrNullDescription() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(" ", 10.0));
        assertThrows(ValidationException.class, () -> useCase.addExpense(null, 10.0));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void addRejectsNullNonFiniteAndNonPositiveAmount() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("A", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome("A", Double.NaN));
        assertThrows(ValidationException.class, () -> useCase.addExpense("A", Double.POSITIVE_INFINITY));
        assertThrows(ValidationException.class, () -> useCase.addExpense("A", 0.0));
        assertThrows(ValidationException.class, () -> useCase.addExpense("A", -5.0));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void removeTransactionSuccessInvalidIdAndNotFound() throws Exception {
        Transaction transaction = useCase.addIncome("Hapus", 10.0);
        useCase.removeTransaction(transaction.getId());
        assertTrue(useCase.getAllTransactions().isEmpty());

        assertThrows(ValidationException.class, () -> useCase.removeTransaction(0));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(-1));

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(99));
        assertEquals(99, ex.getId());
    }

    @Test
    void searchIsCaseInsensitiveTrimmedAndOnlyFilters() throws Exception {
        useCase.addIncome("Gaji Bulanan", 100.0);
        useCase.addExpense("Makan Siang", 20.0);

        List<Transaction> byKeyword = useCase.searchTransactions("  gAJi ");
        assertEquals(1, byKeyword.size());
        assertEquals("Gaji Bulanan", byKeyword.get(0).getDescription());

        assertTrue(useCase.searchTransactions("tidak ada").isEmpty());
        assertEquals(2, useCase.searchTransactions(null).size());
        assertEquals(2, useCase.searchTransactions("").size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortTransactionsUsesSortOptionComparator() throws Exception {
        useCase.addIncome("Besar", 300.0);
        useCase.addExpense("Kecil", 100.0);
        useCase.addIncome("Sedang", 200.0);

        assertEquals("Kecil", useCase.sortTransactions(SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("Besar", useCase.sortTransactions(SortOption.AMOUNT_DESC).get(0).getDescription());
        assertEquals(TransactionType.INCOME, useCase.sortTransactions(SortOption.INCOME_FIRST).get(0).getType());
        assertEquals(TransactionType.EXPENSE, useCase.sortTransactions(SortOption.EXPENSE_FIRST).get(0).getType());
    }

    @Test
    void balanceStaysConsistentAfterAddAndRemove() throws Exception {
        assertEquals(0.0, useCase.getBalance(), 0.0001);

        useCase.addIncome("Gaji", 5_000_000.0);
        Transaction expense = useCase.addExpense("Makan", 25_000.0);
        assertEquals(5_000_000.0, useCase.getTotalIncome(), 0.0001);
        assertEquals(25_000.0, useCase.getTotalExpense(), 0.0001);
        assertEquals(4_975_000.0, useCase.getBalance(), 0.0001);

        useCase.removeTransaction(expense.getId());
        assertEquals(5_000_000.0, useCase.getBalance(), 0.0001);

        useCase.addExpense("Laptop", 6_000_000.0);
        assertEquals(-1_000_000.0, useCase.getBalance(), 0.0001);
    }

    @Test
    void getAllTransactionsReturnsDefensiveCopy() throws Exception {
        useCase.addIncome("A", 1.0);
        useCase.getAllTransactions().clear();
        assertEquals(1, useCase.getAllTransactions().size());
    }
}

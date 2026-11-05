package org.delcom.ifs25045pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TransactionTest {

    @Test
    void constructorSetsFieldsAndDefaultsCreatedAtToNow() {
        Transaction transaction = new Transaction(1, "Gaji", 5_000_000, TransactionType.INCOME);

        assertEquals(1, transaction.getId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(5_000_000, transaction.getAmount(), 0.0001);
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void constructorKeepsGivenCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 11, 1, 8, 0);
        Transaction transaction = new Transaction(2, "Makan", 25_000, TransactionType.EXPENSE, createdAt);

        assertEquals(createdAt, transaction.getCreatedAt());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
    }

    @Test
    void signedAmountIsPositiveForIncomeAndNegativeForExpense() {
        Transaction income = new Transaction(1, "Gaji", 1_000, TransactionType.INCOME);
        Transaction expense = new Transaction(2, "Makan", 400, TransactionType.EXPENSE);

        assertEquals(1_000, income.signedAmount(), 0.0001);
        assertEquals(-400, expense.signedAmount(), 0.0001);
    }

    @Test
    void changeDescriptionAndAmountUpdateFields() {
        Transaction transaction = new Transaction(3, "Lama", 100, TransactionType.EXPENSE);

        transaction.changeDescription("Baru");
        transaction.changeAmount(250);

        assertEquals("Baru", transaction.getDescription());
        assertEquals(250, transaction.getAmount(), 0.0001);
        assertEquals(3, transaction.getId());
    }
}

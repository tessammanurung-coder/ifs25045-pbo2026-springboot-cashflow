package org.delcom.ifs25045pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private List<Transaction> transactions;

    @BeforeEach
    void setUp() {
        transactions = new ArrayList<>();
        transactions.add(new Transaction(1, "A", 300, TransactionType.INCOME));
        transactions.add(new Transaction(2, "B", 100, TransactionType.EXPENSE));
        transactions.add(new Transaction(3, "C", 200, TransactionType.INCOME));
    }

    @Test
    void sortsByAmountAscendingAndDescending() {
        transactions.sort(SortOption.AMOUNT_ASC.comparator());
        assertEquals(List.of(2, 3, 1), ids());

        transactions.sort(SortOption.AMOUNT_DESC.comparator());
        assertEquals(List.of(1, 3, 2), ids());
    }

    @Test
    void sortsByTransactionType() {
        transactions.sort(SortOption.INCOME_FIRST.comparator());
        assertEquals(TransactionType.INCOME, transactions.get(0).getType());
        assertEquals(TransactionType.EXPENSE, transactions.get(2).getType());

        transactions.sort(SortOption.EXPENSE_FIRST.comparator());
        assertEquals(TransactionType.EXPENSE, transactions.get(0).getType());
        assertEquals(TransactionType.INCOME, transactions.get(2).getType());
    }

    private List<Integer> ids() {
        return transactions.stream().map(Transaction::getId).toList();
    }
}

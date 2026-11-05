package org.delcom.ifs25045pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void saveIncomeAndExpenseAssignIncrementalIdsAndTypes() {
        Transaction income = repository.saveIncome("Gaji", 5_000_000);
        Transaction expense = repository.saveExpense("Makan", 25_000);

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findByIdAndDelete() {
        Transaction transaction = repository.saveExpense("Hapus saya", 10);

        Optional<Transaction> found = repository.findById(transaction.getId());
        assertTrue(found.isPresent());
        assertEquals("Hapus saya", found.get().getDescription());

        assertTrue(repository.deleteById(transaction.getId()));
        assertFalse(repository.deleteById(transaction.getId()));
        assertTrue(repository.findById(transaction.getId()).isEmpty());
    }

    @Test
    void findByPredicateAndUpdateKeepsReference() {
        repository.saveExpense("Makan siang", 20_000);
        repository.saveExpense("Transport", 15_000);

        List<Transaction> meals = repository.findBy(t -> t.getDescription().contains("Makan"));
        assertEquals(1, meals.size());

        Transaction meal = meals.get(0);
        meal.changeDescription("Makan malam");
        repository.update(meal);

        assertEquals("Makan malam", repository.findById(meal.getId()).orElseThrow().getDescription());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("A", 1);
        List<Transaction> all = repository.findAll();
        all.clear();
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void saveEntityDirectly() {
        Transaction entity = new Transaction(99, "Manual", 5, TransactionType.INCOME);
        repository.save(entity);
        assertTrue(repository.findById(99).isPresent());
    }
}

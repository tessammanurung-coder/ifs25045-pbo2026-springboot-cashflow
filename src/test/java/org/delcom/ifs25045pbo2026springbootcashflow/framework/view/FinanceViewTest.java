package org.delcom.ifs25045pbo2026springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25045pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25045pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25045pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FinanceViewTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new FinanceUseCase(new TransactionRepository()));
        MockVaadin.setup();
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter()));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void addSearchSortAndDeleteFlow() throws Exception {
        assertTrue(_get(Paragraph.class).getText().contains("Data transaksi belum tersedia"));
        assertSummary("0", "Rp 0.00", "Rp 0.00", "Rp 0.00");

        addTransaction("Tambah Pemasukan", "Gaji", 5_000_000.0);
        assertEquals(1, useCase.getAllTransactions().size());
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());

        addTransaction("Tambah Pengeluaran", "Makan siang", 25_000.0);
        assertEquals(2, useCase.getAllTransactions().size());
        assertSummary("2", "Rp 5,000,000.00", "Rp 25,000.00", "Rp 4,975,000.00");

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Transaksi tidak ditemukan"));

        _setValue(searchField, "GAJI");
        clickButton("Cari");
        assertEquals("Hasil Pencarian: \"GAJI\"", _get(Paragraph.class).getText());

        @SuppressWarnings("unchecked")
        Grid<Transaction> grid = _get(Grid.class);
        assertEquals(1, grid.getListDataView().getItemCount());
        // Pencarian hanya memfilter tampilan, tidak mengubah data maupun saldo.
        assertEquals(2, useCase.getAllTransactions().size());
        assertSummary("2", "Rp 5,000,000.00", "Rp 25,000.00", "Rp 4,975,000.00");

        clickButton("Muat Ulang");
        assertEquals(2, grid.getListDataView().getItemCount());
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());

        sortBy(grid, "amount", SortDirection.ASCENDING);
        assertEquals("Makan siang", firstDescription(grid));
        sortBy(grid, "amount", SortDirection.DESCENDING);
        assertEquals("Gaji", firstDescription(grid));

        sortBy(grid, "type", SortDirection.ASCENDING);
        assertEquals("Gaji", firstDescription(grid));
        sortBy(grid, "type", SortDirection.DESCENDING);
        assertEquals("Makan siang", firstDescription(grid));

        sortBy(grid, "description", SortDirection.ASCENDING);
        assertEquals("Gaji", firstDescription(grid));
        sortBy(grid, "description", SortDirection.DESCENDING);
        assertEquals("Makan siang", firstDescription(grid));

        sortBy(grid, "id", SortDirection.DESCENDING);
        assertEquals("Makan siang", firstDescription(grid));
        // Waktu dicatat hampir bersamaan, jadi urutannya hanya dijalankan (tanpa asersi urutan).
        sortBy(grid, "createdAt", SortDirection.ASCENDING);
        sortBy(grid, "createdAt", SortDirection.DESCENDING);
        assertEquals(2, grid.getListDataView().getItemCount());

        assertEquals("Pemasukan", typeBadge(grid, indexOfDescription(grid, "Gaji")));
        assertEquals("Pengeluaran", typeBadge(grid, indexOfDescription(grid, "Makan siang")));

        openDeleteDialog(grid, indexOfDescription(grid, "Makan siang"));
        clickDialogButton(_get(Dialog.class), "Hapus");
        assertEquals(1, useCase.getAllTransactions().size());
        assertSummary("1", "Rp 5,000,000.00", "Rp 0.00", "Rp 5,000,000.00");
    }

    @Test
    void handlesValidationErrorsCancelAndBlankSearch() {
        clickButton("Tambah Pemasukan");
        Dialog incomeDialog = _get(Dialog.class);
        clickDialogButton(incomeDialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());

        _setValue(_get(incomeDialog, TextField.class), "Gaji");
        clickDialogButton(incomeDialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());

        _setValue(_get(incomeDialog, NumberField.class), -100.0);
        clickDialogButton(incomeDialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());
        clickDialogButton(incomeDialog, "Batal");

        clickButton("Tambah Pengeluaran");
        Dialog expenseDialog = _get(Dialog.class);
        _setValue(_get(expenseDialog, TextField.class), "Makan");
        _setValue(_get(expenseDialog, NumberField.class), 0.0);
        clickDialogButton(expenseDialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());
        clickDialogButton(expenseDialog, "Batal");

        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "   ");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Data transaksi belum tersedia"));
    }

    @Test
    void deleteCanBeCancelled() throws Exception {
        useCase.addIncome("Ada", 10.0);
        clickButton("Muat Ulang");

        @SuppressWarnings("unchecked")
        Grid<Transaction> grid = _get(Grid.class);
        openDeleteDialog(grid, 0);
        clickDialogButton(_get(Dialog.class), "Batal");

        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void handlesNotFoundAndInvalidIdOnDelete() throws Exception {
        useCase.addExpense("Temp", 5.0);
        clickButton("Muat Ulang");

        Transaction selected = useCase.getAllTransactions().get(0);

        @SuppressWarnings("unchecked")
        Grid<Transaction> grid = _get(Grid.class);

        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).removeTransaction(selected.getId());
        openDeleteDialog(grid, 0);
        clickDialogButton(_get(Dialog.class), "Hapus");

        doThrow(new ValidationException("ID transaksi tidak valid"))
                .when(useCase).removeTransaction(selected.getId());
        openDeleteDialog(grid, 0);
        clickDialogButton(_get(Dialog.class), "Hapus");

        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void exposesAccessibleLandmarksAndLabels() {
        FinanceView view = _get(FinanceView.class);
        assertEquals("main", view.getElement().getAttribute("role"));
        assertEquals("status", _get(Paragraph.class).getElement().getAttribute("role"));

        HorizontalLayout summary = (HorizontalLayout) view.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElseThrow();
        assertEquals("region", summary.getElement().getAttribute("role"));
        assertEquals("Ringkasan keuangan", summary.getElement().getAttribute("aria-label"));
        assertEquals("Daftar transaksi", _get(Grid.class).getElement().getAttribute("aria-label"));

        clickButton("Tambah Pemasukan");
        Dialog dialog = _get(Dialog.class);
        assertEquals("Menambah Pemasukan", dialog.getElement().getProperty("ariaLabel"));
        assertTrue(dialog.getThemeNames().contains("no-padding"));
        clickDialogButton(dialog, "Batal");
    }

    private void addTransaction(String buttonCaption, String description, Double amount) {
        clickButton(buttonCaption);
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class), description);
        _setValue(_get(dialog, NumberField.class), amount);
        clickDialogButton(dialog, "Simpan");
    }

    private void openDeleteDialog(Grid<Transaction> grid, int row) {
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, row, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
    }

    private void sortBy(Grid<Transaction> grid, String columnKey, SortDirection direction) {
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(columnKey), direction)));
    }

    private String firstDescription(Grid<Transaction> grid) {
        return grid.getListDataView().getItems().toList().get(0).getDescription();
    }

    private int indexOfDescription(Grid<Transaction> grid, String description) {
        List<Transaction> items = grid.getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (description.equals(items.get(i).getDescription())) {
                return i;
            }
        }
        throw new AssertionError("Transaksi tidak ditemukan: " + description);
    }

    private String typeBadge(Grid<Transaction> grid, int row) {
        return ((Span) _getCellComponent(grid, row, "type")).getText();
    }

    private void assertSummary(String total, String income, String expense, String balance) {
        FinanceView view = _get(FinanceView.class);
        HorizontalLayout summary = (HorizontalLayout) view.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElseThrow();
        List<String> values = summary.getChildren()
                .map(card -> ((Div) card).getChildren()
                        .skip(1)
                        .map(component -> ((Span) component).getText())
                        .findFirst()
                        .orElse(""))
                .toList();
        assertEquals(List.of(total, income, expense, balance), values);
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}

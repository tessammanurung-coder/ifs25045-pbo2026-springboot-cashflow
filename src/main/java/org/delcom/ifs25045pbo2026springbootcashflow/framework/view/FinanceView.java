package org.delcom.ifs25045pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25045pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25045pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25045pbo2026springbootcashflow.framework.web.CashflowAppShell;
import org.delcom.ifs25045pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi cashflow.
 * Tambah pemasukan/pengeluaran lewat dialog, hapus via dialog konfirmasi.
 * Pengurutan dilakukan dengan klik header kolom tabel; kolom jumlah dan tipe
 * memakai comparator dari {@link SortOption} di domain.
 *
 * <p>Aksesibilitas: view ini adalah landmark {@code main}; semua pasangan warna teks/latar
 * memenuhi kontras WCAG AA (&ge; 4,5:1). Tidak memakai ikon Vaadin agar chunk iconset
 * ({@code @vaadin/icons}) tidak ikut terunduh.</p>
 */
@Route("")
@PageTitle(CashflowAppShell.TITLE)
public class FinanceView extends VerticalLayout {

    private static final String NO_DATA_MESSAGE = "- Data transaksi belum tersedia!";
    private static final String NOT_FOUND_MESSAGE = "- Transaksi tidak ditemukan!";

    // Warna solid dengan kontras >= 4,5:1 terhadap teks putih (WCAG 2.2 AA).
    private static final String SOLID_INCOME = "#166534";
    private static final String SOLID_EXPENSE = "#B91C1C";
    private static final String TEXT_ON_SOLID = "#FFFFFF";
    private static final String DELETE_TEXT = "#991B1B";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span(NO_DATA_MESSAGE);
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span("Rp 0.00");
    private final Span totalExpense = new Span("Rp 0.00");
    private final Span balance = new Span("Rp 0.00");

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        // Satu landmark <main> untuk seluruh konten halaman (aturan Axe: landmark-one-main, region).
        getElement().setAttribute("role", "main");

        H1 title = new H1("Cashflow – Catatan Keuangan");

        configureGrid();
        configureToolbar();

        // Perubahan status (hasil cari, daftar kosong) diumumkan ke pembaca layar.
        statusLabel.getElement().setAttribute("role", "status");

        add(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", totalIncome, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", totalExpense, "#FEE2E2", "#DC2626", "#991B1B"),
                createSummaryCard("Saldo", balance, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch").set("flex-wrap", "wrap");
        summary.getElement().setAttribute("role", "region");
        summary.getElement().setAttribute("aria-label", "Ringkasan keuangan");
        return summary;
    }

    private Div createSummaryCard(
            String label,
            Span value,
            String background,
            String accent,
            String valueColor
    ) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", valueColor);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1 1 10rem")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        totalIncome.setText(presenter.formatAmount(financeUseCase.getTotalIncome()));
        totalExpense.setText(presenter.formatAmount(financeUseCase.getTotalExpense()));
        balance.setText(presenter.formatAmount(financeUseCase.getBalance()));
    }

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setWidth("5rem")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId));

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setWidth("12rem")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        // Pengurutan jumlah dan tipe memakai comparator domain (SortOption), bukan logika di view.
        grid.addColumn(transaction -> presenter.formatAmount(transaction.getAmount()))
                .setHeader("Jumlah")
                .setKey("amount")
                .setWidth("11rem")
                .setFlexGrow(0)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setWidth("9rem")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        grid.addColumn(transaction -> presenter.formatCreatedAt(transaction))
                .setHeader("Waktu")
                .setKey("createdAt")
                .setWidth("11rem")
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("tindakan")
                .setWidth("8rem")
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.getElement().setAttribute("aria-label", "Daftar transaksi");
        grid.setSizeFull();
    }

    private Span createTypeBadge(Transaction transaction) {
        boolean income = transaction.getType() == TransactionType.INCOME;
        Span badge = new Span(presenter.formatType(transaction.getType()));
        badge.getStyle()
                .set("background", income ? "#DCFCE7" : "#FEE2E2")
                .set("color", income ? "#166534" : "#991B1B")
                .set("border-radius", "999px")
                .set("padding", "0.15rem 0.7rem")
                .set("font-size", "0.8rem")
                .set("font-weight", "600");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        paint(deleteButton, "transparent", DELETE_TEXT);
        deleteButton.getElement().setAttribute("aria-label", "Hapus transaksi " + transaction.getId());
        return new HorizontalLayout(deleteButton);
    }

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setAriaLabel("Cari keterangan transaksi");
        searchField.setClearButtonVisible(true);
        searchField.setMinWidth("12rem");
    }

    private HorizontalLayout buildToolbar() {
        Button incomeButton = new Button("Tambah Pemasukan",
                event -> openAddDialog("Menambah Pemasukan", DialogTone.INCOME, financeUseCase::addIncome));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        paint(incomeButton, SOLID_INCOME, TEXT_ON_SOLID);

        Button expenseButton = new Button("Tambah Pengeluaran",
                event -> openAddDialog("Menambah Pengeluaran", DialogTone.EXPENSE, financeUseCase::addExpense));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        paint(expenseButton, SOLID_EXPENSE, TEXT_ON_SOLID);

        Button searchButton = new Button("Cari", event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", event -> refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField,
                searchButton,
                refreshButton,
                incomeButton,
                expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        // Tombol turun ke baris berikutnya di layar sempit, bukan meluber ke samping.
        toolbar.getStyle().set("flex-wrap", "wrap");
        toolbar.getElement().setAttribute("role", "group");
        toolbar.getElement().setAttribute("aria-label", "Pencarian dan aksi transaksi");
        return toolbar;
    }

    /** Fungsi penambah transaksi (pemasukan atau pengeluaran) yang disediakan use case. */
    @FunctionalInterface
    private interface TransactionAdder {
        Transaction add(String description, Double amount) throws ValidationException;
    }

    private void openAddDialog(String title, DialogTone tone, TransactionAdder adder) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();

        NumberField amountField = new NumberField("Jumlah (Rp)");
        amountField.setWidthFull();

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);
        form.setSpacing(true);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", event -> {
            try {
                Transaction created = adder.add(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(created) + " — " + currentBalanceText());
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        paint(saveButton, tone.buttonColor, TEXT_ON_SOLID);

        Button cancelButton = new Button("Batal", event -> dialog.close());
        buildDialog(dialog, title, tone, form, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph(
                "Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                        + "\" (ID " + transaction.getId() + ")?"
        );
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        paint(confirmButton, SOLID_EXPENSE, TEXT_ON_SOLID);

        Button cancelButton = new Button("Batal", event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", DialogTone.DANGER, message, cancelButton, confirmButton);
        dialog.open();
    }

    private enum DialogTone {
        // headerColor dipakai teks putih (>= 4,5:1); buttonColor = warna tombol simpan/hapus.
        INCOME("#166534", SOLID_INCOME),
        EXPENSE("#B45309", SOLID_EXPENSE),
        DANGER("#B91C1C", SOLID_EXPENSE);

        private final String headerColor;
        private final String buttonColor;

        DialogTone(String headerColor, String buttonColor) {
            this.headerColor = headerColor;
            this.buttonColor = buttonColor;
        }
    }

    /**
     * Merakit dialog dengan Header berwarna, Body sejajar, dan Footer abu-abu.
     * Pakai Div + warna solid agar background pasti terlihat (bukan VerticalLayout).
     */
    private void buildDialog(
            Dialog dialog,
            String title,
            DialogTone tone,
            Component bodyContent,
            Button... footerButtons
    ) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);
        dialog.addClassName("cashflow-form-dialog");
        // Varian resmi tanpa padding menggantikan manipulasi shadow DOM lewat executeJs.
        dialog.addThemeVariants(DialogVariant.NO_PADDING);
        // Nama aksesibel dialog untuk pembaca layar.
        dialog.getElement().setProperty("ariaLabel", title);

        H2 headerTitle = new H2(title);
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600")
                .set("line-height", "1.4");

        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", tone.headerColor)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("overflow", "hidden")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("align-items", "center")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");

        dialog.removeAll();
        dialog.add(shell);
    }

    /** Warna tombol solid dengan kontras terjamin; dipasang lewat custom property tema. */
    private static void paint(Button button, String background, String text) {
        button.getStyle()
                .set("--vaadin-button-background", background)
                .set("--vaadin-button-text-color", text);
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess() + " " + currentBalanceText());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        emptyState.setText(NOT_FOUND_MESSAGE);
        refreshGrid(financeUseCase.searchTransactions(keyword), "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void refreshAll() {
        emptyState.setText(NO_DATA_MESSAGE);
        refreshGrid(financeUseCase.getAllTransactions(), "Daftar Transaksi");
        searchField.clear();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private String currentBalanceText() {
        return presenter.formatBalance(financeUseCase.getBalance());
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

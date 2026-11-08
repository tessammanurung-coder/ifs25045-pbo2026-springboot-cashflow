package org.delcom.ifs25045pbo2026springbootcashflow.framework.web;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Konfigurasi halaman (app shell) Vaadin: judul awal, meta description, dan favicon.
 *
 * <p>Catatan penting: tanpa AppShellConfigurator, Vaadin 25 otomatis memuat tema Aura.
 * Begitu kelas ini ada, pemuatan otomatis itu berhenti, sehingga tema harus dinyatakan
 * eksplisit lewat {@code @StyleSheet} agar tampilan aplikasi tetap sama.</p>
 */
@StyleSheet(Aura.STYLESHEET)
@Meta(name = "description", content = CashflowAppShell.DESCRIPTION)
public class CashflowAppShell implements AppShellConfigurator {

    public static final String TITLE = "Cashflow - Catatan Keuangan";

    static final String DESCRIPTION = "Aplikasi Cashflow untuk mencatat dan mengelola pemasukan, "
            + "pengeluaran, serta memantau saldo keuangan secara praktis.";

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle(TITLE);
        // Ikon eksplisit mencegah browser meminta /favicon.ico (404 -> error di konsol).
        settings.addFavIcon("icon", "favicon.svg", "any");
    }
}

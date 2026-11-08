package org.delcom.ifs25045pbo2026springbootcashflow.framework.web;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.aura.Aura;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CashflowAppShellTest {

    @Test
    void declaresDescriptionMetaForSeo() {
        Meta[] metas = CashflowAppShell.class.getAnnotationsByType(Meta.class);
        assertEquals(1, metas.length);
        assertEquals("description", metas[0].name());
        assertTrue(metas[0].content().contains("pemasukan"));
        assertTrue(metas[0].content().length() >= 50 && metas[0].content().length() <= 160);
    }

    @Test
    void loadsAuraThemeExplicitlySoLookDoesNotChange() {
        StyleSheet[] sheets = CashflowAppShell.class.getAnnotationsByType(StyleSheet.class);
        assertEquals(1, sheets.length);
        assertEquals(Aura.STYLESHEET, sheets[0].value());
    }

    @Test
    void configurePageSetsTitleAndFavIconWithoutError() {
        assertEquals("Cashflow - Catatan Keuangan", CashflowAppShell.TITLE);
        assertDoesNotThrow(() -> new CashflowAppShell().configurePage(new AppShellSettings()));
    }
}

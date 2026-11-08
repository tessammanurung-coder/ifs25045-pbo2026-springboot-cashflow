package org.delcom.ifs25045pbo2026springbootcashflow.framework.web;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndonesianI18NProviderTest {

    private final IndonesianI18NProvider provider = new IndonesianI18NProvider();

    @Test
    void providesOnlyIndonesianLocale() {
        assertEquals(List.of(IndonesianI18NProvider.INDONESIAN), provider.getProvidedLocales());
        assertEquals("id", IndonesianI18NProvider.INDONESIAN.getLanguage());
    }

    @Test
    void translationReturnsKeyAsIs() {
        assertEquals("Simpan", provider.getTranslation("Simpan", Locale.ENGLISH));
        assertEquals("Simpan", provider.getTranslation("Simpan", IndonesianI18NProvider.INDONESIAN, 1, 2));
    }
}

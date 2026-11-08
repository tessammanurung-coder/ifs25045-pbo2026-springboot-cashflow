package org.delcom.ifs25045pbo2026springbootcashflow.framework.web;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Menetapkan bahasa Indonesia sebagai satu-satunya locale aplikasi.
 * Vaadin mengisi atribut {@code <html lang>} dari locale ini; tanpa provider, nilainya
 * mengikuti locale server/browser (mis. "en") sehingga tidak sesuai bahasa antarmuka.
 */
@Component
public class IndonesianI18NProvider implements I18NProvider {

    public static final Locale INDONESIAN = Locale.forLanguageTag("id");

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(INDONESIAN);
    }

    /** Teks antarmuka ditulis langsung dalam bahasa Indonesia, jadi kunci dikembalikan apa adanya. */
    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        return key;
    }
}

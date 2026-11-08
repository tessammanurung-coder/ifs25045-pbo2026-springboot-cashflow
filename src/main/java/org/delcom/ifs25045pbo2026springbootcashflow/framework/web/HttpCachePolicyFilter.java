package org.delcom.ifs25045pbo2026springbootcashflow.framework.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * Menetapkan kebijakan cache dan header keamanan dasar untuk setiap respons.
 *
 * <ul>
 *   <li>{@code /VAADIN/build/**}: nama file memuat hash isi, aman di-cache 1 tahun (immutable).</li>
 *   <li>Tema ({@code /aura}, {@code /lumo}): berubah hanya saat upgrade Vaadin, cache 30 hari.</li>
 *   <li>Aset statis lain (js/css/font/gambar): cache 1 hari.</li>
 *   <li>Selain itu (halaman, UIDL/XHR, data keuangan): {@code no-store}, tidak pernah disimpan.</li>
 * </ul>
 *
 * Vaadin menulis header Cache-Control sendiri (1 jam untuk aset statis); wrapper di bawah
 * mengunci nilai yang sudah ditetapkan di sini agar tidak ditimpa.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpCachePolicyFilter implements Filter {

    static final String CACHE_CONTROL = "Cache-Control";
    static final String IMMUTABLE = "public, max-age=31536000, immutable";
    static final String THEME = "public, max-age=2592000";
    static final String STATIC = "public, max-age=86400";
    static final String NO_STORE = "no-store";

    private static final Set<String> STATIC_EXTENSIONS =
            Set.of("js", "css", "woff2", "woff", "svg", "png", "ico", "webp", "map");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader(CACHE_CONTROL, cacheControlFor(pathOf(httpRequest)));
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        httpResponse.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
        httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        chain.doFilter(request, new LockedCacheControlResponse(httpResponse));
    }

    /** Nilai Cache-Control untuk sebuah path (tanpa context path dan query). */
    static String cacheControlFor(String path) {
        if (path.startsWith("/VAADIN/build/")) {
            return IMMUTABLE;
        }
        if (!isStaticFile(path)) {
            return NO_STORE;
        }
        return path.startsWith("/aura/") || path.startsWith("/lumo/") ? THEME : STATIC;
    }

    private static boolean isStaticFile(String path) {
        int lastDot = path.lastIndexOf('.');
        int lastSlash = path.lastIndexOf('/');
        return lastDot > lastSlash && STATIC_EXTENSIONS.contains(path.substring(lastDot + 1));
    }

    private static String pathOf(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /** Mengabaikan upaya servlet lain mengubah Cache-Control yang sudah diputuskan filter ini. */
    static final class LockedCacheControlResponse extends HttpServletResponseWrapper {
        LockedCacheControlResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            if (!CACHE_CONTROL.equalsIgnoreCase(name)) {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if (!CACHE_CONTROL.equalsIgnoreCase(name)) {
                super.addHeader(name, value);
            }
        }
    }
}

package org.delcom.ifs25045pbo2026springbootcashflow.framework.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpCachePolicyFilterTest {

    private final HttpCachePolicyFilter filter = new HttpCachePolicyFilter();

    private static HttpServletRequest request(String contextPath, String uri) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpCachePolicyFilterTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getRequestURI" -> uri;
                    case "getContextPath" -> contextPath;
                    default -> null;
                });
    }

    /** Respons tiruan yang mencatat header yang akhirnya benar-benar ditulis. */
    private static HttpServletResponse response(Map<String, List<String>> headers) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpCachePolicyFilterTest.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("setHeader")) {
                        headers.put((String) args[0], new ArrayList<>(List.of((String) args[1])));
                    } else if (method.getName().equals("addHeader")) {
                        headers.computeIfAbsent((String) args[0], k -> new ArrayList<>()).add((String) args[1]);
                    }
                    return null;
                });
    }

    @Test
    void hashedBuildAssetsAreImmutableForOneYear() {
        assertEquals("public, max-age=31536000, immutable",
                HttpCachePolicyFilter.cacheControlFor("/VAADIN/build/vaadin-bundle-abc123.js"));
    }

    @Test
    void themeAssetsAreCachedThirtyDays() {
        assertEquals("public, max-age=2592000", HttpCachePolicyFilter.cacheControlFor("/aura/aura.css"));
        assertEquals("public, max-age=2592000",
                HttpCachePolicyFilter.cacheControlFor("/aura/fonts/InstrumentSans/InstrumentSans.woff2"));
        assertEquals("public, max-age=2592000", HttpCachePolicyFilter.cacheControlFor("/lumo/lumo.css"));
    }

    @Test
    void otherStaticFilesAreCachedOneDay() {
        assertEquals("public, max-age=86400", HttpCachePolicyFilter.cacheControlFor("/favicon.svg"));
        assertEquals("public, max-age=86400", HttpCachePolicyFilter.cacheControlFor("/VAADIN/static/app.js"));
    }

    @Test
    void pagesAndDynamicRequestsAreNeverStored() {
        assertEquals("no-store", HttpCachePolicyFilter.cacheControlFor("/"));
        assertEquals("no-store", HttpCachePolicyFilter.cacheControlFor("/VAADIN/push"));
        assertEquals("no-store", HttpCachePolicyFilter.cacheControlFor("/robots.txt"));
        // Titik hanya di nama direktori, bukan ekstensi file.
        assertEquals("no-store", HttpCachePolicyFilter.cacheControlFor("/v1.2/data"));
        assertEquals("no-store", HttpCachePolicyFilter.cacheControlFor("/laporan.pdf"));
    }

    @Test
    void filterSetsPolicyAndSecurityHeadersUsingPathWithoutContextPath() throws Exception {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        boolean[] chained = {false};
        FilterChain chain = (req, res) -> chained[0] = true;

        filter.doFilter(request("/app", "/app/VAADIN/build/x-1.js"), response(headers), chain);

        assertTrue(chained[0]);
        assertEquals(List.of("public, max-age=31536000, immutable"), headers.get("Cache-Control"));
        assertEquals(List.of("nosniff"), headers.get("X-Content-Type-Options"));
        assertEquals(List.of("strict-origin-when-cross-origin"), headers.get("Referrer-Policy"));
        assertEquals(List.of("SAMEORIGIN"), headers.get("X-Frame-Options"));
        assertEquals(List.of("frame-ancestors 'self'"), headers.get("Content-Security-Policy"));
        assertEquals(List.of("same-origin"), headers.get("Cross-Origin-Opener-Policy"));
        assertEquals(List.of("camera=(), microphone=(), geolocation=()"), headers.get("Permissions-Policy"));
    }

    @Test
    void downstreamCannotOverrideCacheControlButOtherHeadersPass() throws Exception {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        FilterChain chain = (req, res) -> {
            HttpServletResponse downstream = (HttpServletResponse) res;
            downstream.setHeader("cache-control", "max-age=3600");
            downstream.addHeader("Cache-Control", "public");
            downstream.setHeader("X-Custom", "1");
            downstream.addHeader("X-Custom", "2");
        };

        filter.doFilter(request("", "/"), response(headers), chain);

        assertEquals(List.of("no-store"), headers.get("Cache-Control"));
        assertEquals(List.of("1", "2"), headers.get("X-Custom"));
    }
}

package org.delcom.ifs25045pbo2026springbootcashflow.domain.exception;

/**
 * Checked exception dasar untuk hierarki domain model aplikasi Cashflow.
 */
public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

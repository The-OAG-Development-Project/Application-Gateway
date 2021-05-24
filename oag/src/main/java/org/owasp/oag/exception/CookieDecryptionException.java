package org.owasp.oag.exception;

/**
 * Thrown whenever a cooky can not be decrypted, this is usually a user error /attack.
 */
public class CookieDecryptionException extends ApplicationException {

    public CookieDecryptionException(String message) {
        super(message);
    }

    public CookieDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

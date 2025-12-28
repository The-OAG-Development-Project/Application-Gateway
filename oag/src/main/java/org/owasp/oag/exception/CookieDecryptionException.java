package org.owasp.oag.exception;

/**
 * Thrown whenever a cooky can not be decrypted, this is usually a user error /attack.
 */
public class CookieDecryptionException extends ApplicationException {

    /**
     * Constructor for CookieDecryptionException.
     *
     * @param message The message to log.
     */
    public CookieDecryptionException(String message) {
        super(message);
    }

    /**
     * Constructor for CookieDecryptionException.
     *
     * @param message The message to log.
     * @param cause   The cause of the exception.
     */
    public CookieDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

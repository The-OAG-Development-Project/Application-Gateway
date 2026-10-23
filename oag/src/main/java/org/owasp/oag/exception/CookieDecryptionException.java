package org.owasp.oag.exception;

/**
 * Thrown whenever a cooky can not be decrypted, this is usually a user error /attack.
 * <p>
 * Logs at debug level on purpose. An undecryptable cookie is attacker-controlled and arrives on the
 * request path, so it must not let a caller inflate the log at a higher level. Every handler of this
 * exception already reports the event once at info: {@code ExtractAuthenticationFilter} logs
 * "Received invalid session cookie" with the request's trace id, and the login-state path turns it
 * into a 401 whose reason {@code SimpleLogFilter} logs. The debug entry adds the stack trace for
 * diagnosis without duplicating those.
 */
public class CookieDecryptionException extends ApplicationException {

    /**
     * Constructor for CookieDecryptionException.
     *
     * @param message The message to log.
     */
    public CookieDecryptionException(String message) {
        super(message, false);
    }

    /**
     * Constructor for CookieDecryptionException.
     *
     * @param message The message to log.
     * @param cause   The cause of the exception.
     */
    public CookieDecryptionException(String message, Throwable cause) {
        super(message, cause, false);
    }
}

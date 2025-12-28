package org.owasp.oag.exception;

/**
 * Used for all cases where authentication fails.
 */
public class AuthenticationException extends ApplicationException {

    /**
     * Creates a new AuthenticationException with the given message.
     *
     * @param message The message to log.
     */
    public AuthenticationException(String message) {
        super(message);
    }

}

package org.owasp.oag.exception;

/**
 * Used for all cases where authentication fails.
 */
public class AuthenticationException extends ApplicationException {

    public AuthenticationException(String message) {
        super(message, null);
    }

}

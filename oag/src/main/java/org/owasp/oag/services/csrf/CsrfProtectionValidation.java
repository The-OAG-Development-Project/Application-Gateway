package org.owasp.oag.services.csrf;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for CSRF (Cross-Site Request Forgery) protection validation.
 * Implementations of this interface provide different strategies for validating
 * requests to protect against CSRF attacks.
 */
public interface CsrfProtectionValidation {

    /**
     * Indicates whether this validation strategy requires access to the request body.
     *
     * @return true if the request body is needed for validation, false otherwise
     */
    boolean needsRequestBody();

    /**
     * Determines whether a request should be blocked based on CSRF validation.
     *
     * @param request The current request
     * @param requestBody The request body content, if available
     * @return true if the request should be blocked due to CSRF validation failure, false otherwise
     */
    boolean shouldBlockRequest(HttpServletRequest request, String requestBody);
}

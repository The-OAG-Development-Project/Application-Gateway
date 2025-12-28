package org.owasp.oag.utils;

/**
 * Contains constants that are shared across multiple components of the application.
 * This class provides centralized access to common constants to ensure consistency
 * throughout the codebase.
 */
public final class SharedConstants {
    /**
     * The URI used as JWKS endpoint. (To get signing keys).
     * This constant defines the well-known URI path for the JSON Web Key Set (JWKS),
     * following the standard convention for JWKS endpoints.
     */
    public static final String JWKS_BASE_URI = "/.well-known/jwks";
}

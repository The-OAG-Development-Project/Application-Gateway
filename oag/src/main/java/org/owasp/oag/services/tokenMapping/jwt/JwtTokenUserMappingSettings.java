package org.owasp.oag.services.tokenMapping.jwt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.services.tokenMapping.UserMappingTemplatingEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration settings for JWT token user mapping.
 * This class holds all the configuration parameters needed for creating and
 * validating JWT tokens used for downstream authentication.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtTokenUserMappingSettings {

    /**
     * The name of the HTTP header where the JWT will be placed.
     */
    public String headerName;
    
    /**
     * The prefix to add before the JWT in the header (e.g., "Bearer ").
     */
    public String headerPrefix;
    
    /**
     * The audience claim value for the JWT.
     */
    public String audience;
    
    /**
     * The issuer claim value for the JWT.
     */
    public String issuer;
    
    /**
     * The lifetime of the JWT token in seconds.
     */
    public int tokenLifetimeSeconds;
    
    /**
     * The name of the signature implementation to use for signing JWTs.
     */
    public String signatureImplementation;
    
    /**
     * Implementation-specific settings for the signature algorithm.
     */
    public Map<String, Object> signatureSettings = new HashMap<>();
    
    /**
     * Mappings from claim names to template expressions for populating JWT claims.
     */
    public Map<String, String> mappings = new HashMap<>();

    /**
     * Default constructor.
     */
    public JwtTokenUserMappingSettings() {
    }

    /**
     * Constructs a new JwtTokenUserMappingSettings with the specified parameters.
     *
     * @param headerName The name of the HTTP header where the JWT will be placed
     * @param headerPrefix The prefix to add before the JWT in the header
     * @param audience The audience claim value for the JWT
     * @param issuer The issuer claim value for the JWT
     * @param tokenLifetimeSeconds The lifetime of the JWT token in seconds
     * @param signatureImplementation The signature implementation to use
     * @param signatureSettings Implementation-specific settings for the signature algorithm
     * @param mappings Mappings from claim names to template expressions
     */
    public JwtTokenUserMappingSettings(String headerName, String headerPrefix, String audience, String issuer, int tokenLifetimeSeconds, String signatureImplementation, Map<String, Object> signatureSettings, Map<String, String> mappings) {
        this.headerName = headerName;
        this.headerPrefix = headerPrefix;
        this.audience = audience;
        this.issuer = issuer;
        this.tokenLifetimeSeconds = tokenLifetimeSeconds;
        this.signatureImplementation = signatureImplementation;
        this.signatureSettings = signatureSettings;
        this.mappings = mappings;
    }

    /**
     * Validates the current settings.
     * 
     * @throws ConfigurationException if any of the required settings are invalid
     */
    public void requireValidSettings() {

        if (this.headerName == null || "".equals(this.headerName))
            throw new ConfigurationException("Config: JwtTokenMapper headerName is invalid", null);

        if (this.headerPrefix == null)
            throw new ConfigurationException("Config: JwtTokenMapper headerPrefix is invalid", null);

        if (this.audience == null)
            throw new ConfigurationException("Config: JwtTokenMapper audience is invalid", null);

        if (this.issuer == null)
            throw new ConfigurationException("Config: JwtTokenMapper issuer is invalid", null);

        if (this.signatureImplementation == null)
            throw new ConfigurationException("Config: JwtTokenMapper signatureImplementation is invalid", null);

        if (this.mappings == null)
            throw new ConfigurationException("Config: JwtTokenMapper mappings is invalid", null);

        for (var entry : this.mappings.entrySet()) {
            if (!UserMappingTemplatingEngine.isValidTemplate(entry.getValue()))
                throw new ConfigurationException("Config: JwtTokenMapper invalid mapping: " + entry.getKey() + " -> " + entry.getValue(), null);
        }
    }
}
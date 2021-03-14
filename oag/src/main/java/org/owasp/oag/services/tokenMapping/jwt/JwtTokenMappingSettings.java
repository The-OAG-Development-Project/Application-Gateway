package org.owasp.oag.services.tokenMapping.jwt;

import java.util.Map;

public class JwtTokenMappingSettings{

    public String headerName;
    public String headerPrefix;
    public String audience;
    public String issuer;
    public int tokenLifetimeSeconds;
    public String signatureImplementation;
    public Map<String, Object> signatureSettings;
    public Map<String, String> mappings;

    public JwtTokenMappingSettings() {
    }

    public JwtTokenMappingSettings(String headerName, String headerPrefix, String audience, String issuer, int tokenLifetimeSeconds, String signatureImplementation, Map<String, Object> signatureSettings) {
        this.headerName = headerName;
        this.headerPrefix = headerPrefix;
        this.audience = audience;
        this.issuer = issuer;
        this.tokenLifetimeSeconds = tokenLifetimeSeconds;
        this.signatureImplementation = signatureImplementation;
        this.signatureSettings = signatureSettings;
    }

    public void requireValidSettings(){

        if (this.headerName == null || "".equals(this.headerName))
            throw new RuntimeException("Config: JwtTokenMapper headerName is invalid");

        if (this.headerPrefix == null)
            throw new RuntimeException("Config: JwtTokenMapper headerPrefix is invalid");

        if (this.audience == null)
            throw new RuntimeException("Config: JwtTokenMapper audience is invalid");

        if (this.issuer == null)
            throw new RuntimeException("Config: JwtTokenMapper issuer is invalid");

        if (this.signatureImplementation == null)
            throw new RuntimeException("Config: JwtTokenMapper signatureImplementation is invalid");
    }
}
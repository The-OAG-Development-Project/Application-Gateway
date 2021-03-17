package org.owasp.oag.controllers;

import com.nimbusds.jose.jwk.JWKSet;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.infrastructure.factories.KeyManagementFactory;
import org.owasp.oag.services.keymgm.JwkStore;
import org.owasp.oag.utils.LoggingUtils;
import org.owasp.oag.utils.ReactiveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Provides "public" (to down stream systems) access to the JWT signer keys currently in use.
 * It does this as a well-known URI (defined in https://tools.ietf.org/html/rfc8615) yet uses
 * a non-registered endpoint of jwks (i.e. server.com/.well-known/jwks).
 */
@RestController
public class JwksController {
    private static final String JWKS_BASE_URI = "/.well-known/jwks";

    private static final Logger log = LoggerFactory.getLogger(JwksController.class);

    private final JwkStore jwkStore;
    private final MainConfig config;

    @Autowired
    JwksController(MainConfig config, KeyManagementFactory keyManagementFactory) {
        this.config = config;
        jwkStore = keyManagementFactory.getJWKStore();
    }

    /**
     * Provides all the JWT signing keys used by OAG in the format of a JWK Set specified at https://tools.ietf.org/html/rfc7517.
     * Namely this means:
     * <code>
     * {"keys":
     * [
     * {"kty":"RSA",
     * "n": "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx
     * 4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs
     * tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2
     * QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI
     * SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb
     * w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
     * "e":"AQAB",
     * "alg":"RS256",
     * "use": "sig",
     * "kid":"gdfgrgergt-fzrfzgf-4343-utft"}
     * },
     * {...}
     * ]
     * }
     * </code>
     *
     * @return all currently valid OAG used JWT signing keys as JWK Set. Returns an empty array when there are no such keys.
     */
    @GetMapping(JWKS_BASE_URI)
    public Mono<ResponseEntity<Map<String, ?>>> getAllSigningKeys() {
        return ReactiveUtils.runBlockingProcedure(() -> {
            LoggingUtils.contextual(() -> log.debug("Retrieving all available JWK ..."));
            JWKSet keys = jwkStore.getSigningPublicKeys();
            LoggingUtils.contextual(() -> log.debug("signing Keys successfully retrieved..."));
            // note: JWKSet keys is never null and always return a proper empty array when no keys are there.
            return keys.toJSONObject();
        })
                .map(response -> {
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * @param kid the key id of the JWT signing key requested.
     * @return the OAG JWT signing key with passed in kid (key id) (as single entry in the JWK Set array). Returns an empty array when not found.
     */
    @GetMapping(JWKS_BASE_URI + "/{kid}")
    public Mono<ResponseEntity<Map<String, ?>>> getSigningKeys(@PathVariable("kid") String kid) {
        return ReactiveUtils.runBlockingProcedure(() -> {
            LoggingUtils.contextual(() -> log.debug("Retrieving singing JWK with kid {}.", kid));
            JWKSet key = jwkStore.getSigningPublicKey(kid);
            LoggingUtils.contextual(() -> log.debug("signing Key {} successfully retrieved: {}.", kid, key.getKeyByKeyId(kid) != null));
            return key.toJSONObject();
        })
                .map(response -> {
                    return ResponseEntity.ok(response);
                });
    }
}

package org.owasp.oag.services.crypto.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.exception.ConsistencyException;
import org.owasp.oag.services.keymgm.CurrentSigningKeyHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.owasp.oag.services.crypto.jwt.JwtSignerFactory.JWT_SIGNER_FACTORY_BEAN_POSTFIX;

@Component("rsa" + JWT_SIGNER_FACTORY_BEAN_POSTFIX)
public class RSAJwtSignerFactory implements JwtSignerFactory {

    @Autowired
    CurrentSigningKeyHolder keyHolder;

    public JwtSigner create(String hostUri, Map<String, Object> settings) {

        if (keyHolder.getCurrentPrivateKey() == null) {
            throw new ConfigurationException("RsaJwtSignerFactory Invalid configuration: No auto key rotation applied, no current private key exists.", null);
        }

        if (hostUri == null) {
            throw new ConsistencyException("hostUri must be provided.");
        }

        return new RsaJwtSigner(keyHolder.getCurrentPrivateKey(), keyHolder.getKid(), JWSAlgorithm.RS256, hostUri);
    }
}

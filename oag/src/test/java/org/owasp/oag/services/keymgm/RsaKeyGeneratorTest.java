package org.owasp.oag.services.keymgm;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.KeyGeneratorProfile;
import org.owasp.oag.config.configuration.KeyManagementProfile;
import org.owasp.oag.config.configuration.MainConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RsaKeyGeneratorTest {

    @Test
    public void generatorTest() {
        MainConfig config = new MainConfig();
        config.setKeyManagementProfile(new KeyManagementProfile());
        config.getKeyManagementProfile().setKeyGeneratorProfile(new KeyGeneratorProfile());
        config.getKeyManagementProfile().getKeyGeneratorProfile().setKeySize(2048);

        RsaKeyGenerator underTest = new RsaKeyGenerator(config);

        KeyGenerator.GeneratedKey key = underTest.generateJWTSigningKey();
        assertNotNull(key);
        assertNotNull(key.keyPair);
        assertNotNull(key.getKeyUsedToVerify());
    }

}
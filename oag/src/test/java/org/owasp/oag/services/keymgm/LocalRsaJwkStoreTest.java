package org.owasp.oag.services.keymgm;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.KeyManagementProfile;
import org.owasp.oag.config.configuration.KeyRotationProfile;
import org.owasp.oag.config.configuration.MainConfig;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LocalRsaJwkStoreTest {

    private final long enoughTime = System.currentTimeMillis() + 1 * 60 * 1000;
    private final long alreadyExpired = System.currentTimeMillis() - 11 * 60 * 1000;

    @Test
    void addGetRemoveKeyTest() throws NoSuchAlgorithmException {

        MainConfig config = new MainConfig();
        config.setKeyManagementProfile(new KeyManagementProfile());
        config.getKeyManagementProfile().setKeyRotationProfile(new KeyRotationProfile());
        config.getKeyManagementProfile().getKeyRotationProfile().setCleanupFrequencySeconds(1000); // never run

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();

        LocalRsaJwkStore underTest = new LocalRsaJwkStore(config, scheduler);

        String kid1 = "testchen1";
        String kid2 = "testchen2";

        KeyPairGenerator factory = KeyPairGenerator.getInstance("RSA");
        factory.initialize(4096);
        KeyPair key1 = factory.generateKeyPair();

        JWK key2 = generateJwkFromNewPubKey(factory, kid2);

        underTest.addRawKey(kid1, key1.getPublic(), enoughTime);
        assertEquals(1, underTest.getSigningPublicKeys().getKeys().size());
        assertNotNull(underTest.getSigningPublicKey(kid1));

        underTest.addJwk(key2, enoughTime);
        assertEquals(2, underTest.getSigningPublicKeys().getKeys().size());
        assertNotNull(underTest.getSigningPublicKey(kid2));

        underTest.removeKey(kid1);
        assertEquals(1, underTest.getSigningPublicKeys().getKeys().size());

        underTest.removeKey(kid2);
        assertEquals(0, underTest.getSigningPublicKeys().getKeys().size());
    }

    @NotNull
    private static RSAKey generateJwkFromNewPubKey(KeyPairGenerator factory, String kid) {
        return new RSAKey.Builder((RSAPublicKey) factory.generateKeyPair().getPublic()).keyID(kid).algorithm(Algorithm.parse("RS256")).keyUse(KeyUse.SIGNATURE).build();
    }

    @Test
    void cleanupTest() throws NoSuchAlgorithmException, InterruptedException {
        MainConfig config = new MainConfig();
        config.setKeyManagementProfile(new KeyManagementProfile());
        config.getKeyManagementProfile().setKeyRotationProfile(new KeyRotationProfile());
        config.getKeyManagementProfile().getKeyRotationProfile().setCleanupFrequencySeconds(1); // run soon

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        LocalRsaJwkStore underTest = new LocalRsaJwkStore(config, scheduler);

        String kid1 = "kid1";
        String kid2 = "kid2";

        KeyPairGenerator factory = KeyPairGenerator.getInstance("RSA");
        factory.initialize(4096);
        KeyPair key1 = factory.generateKeyPair();

        JWK key2 = generateJwkFromNewPubKey(factory, kid2);

        underTest.addRawKey(kid1, key1.getPublic(), alreadyExpired);
        assertEquals(1, underTest.getSigningPublicKeys().getKeys().size());
        assertNotNull(underTest.getSigningPublicKey(kid1));

        underTest.addJwk(key2, enoughTime);
        assertEquals(2, underTest.getSigningPublicKeys().getKeys().size());
        assertNotNull(underTest.getSigningPublicKey(kid2));

        Thread.sleep(2000); // wait for cleanup to run

        assertEquals(1, underTest.getSigningPublicKeys().getKeys().size());
        assertEquals(1, underTest.getSigningPublicKey(kid2).getKeys().size());
        assertEquals(0, underTest.getSigningPublicKey(kid1).getKeys().size());

        underTest.removeKey(kid2);
        assertEquals(0, underTest.getSigningPublicKeys().getKeys().size());
    }
}
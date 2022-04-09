package org.owasp.oag.services.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.exception.ConfigurationException;
import org.owasp.oag.exception.ConsistencyException;
import org.owasp.oag.exception.CookieDecryptionException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Base64;

public class JweEncrypter implements CookieEncryptor {
    private static final SecretKey currentSecretKey;

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            currentSecretKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new ConsistencyException("Cloud not create AES key", e);
        }
    }

    private final SecretKey secretKey;

    private JweEncrypter(byte[] keyBytes) {
        this.secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    public synchronized static JweEncrypter loadInMemoryInstance() throws IOException {
            try {
                byte[] keyBytes = currentSecretKey.getEncoded();
                return new JweEncrypter(keyBytes);
            } catch (Exception e) {
                throw new ConsistencyException("Failed to create JweEncryptor due to bad AESKey.", e);
            }
    }

    public static JweEncrypter loadFromEnvironmentVariable(String variableName) {

        String key = System.getenv(variableName);

        if (key == null)
            throw new ConfigurationException("JWE encryption key is not defined", null);

        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new JweEncrypter(decodedKey);
    }

    private String generateKey() {

        try {
            // Generate symmetric 128 bit AES key
            SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());

        } catch (NoSuchAlgorithmException e) {
            throw new ConsistencyException("AES key could not be generated", e);
        }
    }

    @Override
    public String encryptObject(Object payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String payloadString = objectMapper.writeValueAsString(payload);
            return encrypt(payloadString);

        } catch (JsonProcessingException e) {
            throw new ConsistencyException("Could not encode Json", e);
        }
    }

    private String encrypt(String payload) {

        try {
            // Create JWT
            JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128GCM);
            Payload jwePayload = new Payload(payload);
            JWEObject jweObject = new JWEObject(header, jwePayload);
            jweObject.encrypt(new DirectEncrypter(secretKey));

            // Serialise to compact JOSE form
            return jweObject.serialize();

        } catch (JOSEException e) {
            throw new ConfigurationException("JWE could not be encrypted", e);
        }
    }

    @Override
    public <T> T decryptObject(String jwe, Class<T> clazz) throws CookieDecryptionException {
        String payload = decrypt(jwe);


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payload, clazz);
        } catch (JsonProcessingException e) {
            throw new CookieDecryptionException("JWE could not be deserialized", e);
        }
    }

    private String decrypt(String jwe) throws CookieDecryptionException {
        try {
            // Parse into JWE object again...
            JWEObject jweObject = JWEObject.parse(jwe);

            // Decrypt
            jweObject.decrypt(new DirectDecrypter(secretKey));

            // Get the plain text
            Payload payload = jweObject.getPayload();
            return payload.toString();
        } catch (JOSEException | ParseException e) {
            throw new CookieDecryptionException("JWE could not be decrypted", e);
        }
    }
}

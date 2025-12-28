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

/**
 * Implementation of CookieEncryptor using JWE (JSON Web Encryption) to securely
 * encrypt and decrypt data, especially for cookie storage.
 * This class uses AES-256 GCM encryption.
 */
public class JweEncrypter implements CookieEncryptor {
    
    /**
     * Default secret key used for in-memory encryption.
     */
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

    /**
     * The secret key used for encryption and decryption.
     */
    private final SecretKey secretKey;

    /**
     * Constructs a new JweEncrypter with the specified key bytes.
     * 
     * @param keyBytes The bytes for the AES secret key
     */
    private JweEncrypter(byte[] keyBytes) {
        this.secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    /**
     * Creates a JweEncrypter instance using an in-memory generated key.
     * This should only be used for development or testing purposes.
     *
     * @return A new JweEncrypter instance
     * @throws IOException If an I/O error occurs
     */
    public synchronized static JweEncrypter loadInMemoryInstance() throws IOException {
            try {
                byte[] keyBytes = currentSecretKey.getEncoded();
                return new JweEncrypter(keyBytes);
            } catch (Exception e) {
                throw new ConsistencyException("Failed to create JweEncryptor due to bad AESKey.", e);
            }
    }

    /**
     * Creates a JweEncrypter instance using a key stored in an environment variable.
     * 
     * @param variableName The name of the environment variable containing the Base64-encoded key
     * @return A new JweEncrypter instance
     * @throws ConfigurationException If the environment variable is not defined
     */
    public static JweEncrypter loadFromEnvironmentVariable(String variableName) {

        String key = System.getenv(variableName);

        if (key == null)
            throw new ConfigurationException("JWE encryption key is not defined", null);

        // decode the base64 encoded string
        byte[] decodedKey = Base64.getDecoder().decode(key);
        return new JweEncrypter(decodedKey);
    }

    /**
     * Generates a new random AES key and returns it as a Base64-encoded string.
     * 
     * @return A Base64-encoded AES key
     * @throws ConsistencyException If the key cannot be generated
     */
    private String generateKey() {

        try {
            // Generate symmetric 128 bit AES key
            SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());

        } catch (NoSuchAlgorithmException e) {
            throw new ConsistencyException("AES key could not be generated", e);
        }
    }

    /**
     * Encrypts an object by serializing it to JSON and then encrypting the resulting string.
     * 
     * @param payload The object to encrypt
     * @return The encrypted object as a JWE string
     * @throws ConsistencyException If the object cannot be serialized to JSON
     */
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

    /**
     * Encrypts a string payload using JWE with AES-256-GCM.
     * 
     * @param payload The string to encrypt
     * @return The encrypted string as a JWE
     * @throws ConfigurationException If the encryption fails
     */
    private String encrypt(String payload) {

        try {
            // Create JWT
            JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM);
            Payload jwePayload = new Payload(payload);
            JWEObject jweObject = new JWEObject(header, jwePayload);
            jweObject.encrypt(new DirectEncrypter(secretKey));

            // Serialise to compact JOSE form
            return jweObject.serialize();

        } catch (JOSEException e) {
            throw new ConfigurationException("JWE could not be encrypted", e);
        }
    }

    /**
     * Decrypts a JWE string and deserializes the content to an object of the specified class.
     * 
     * @param <T> The type of object to deserialize to
     * @param jwe The JWE string to decrypt
     * @param clazz The class of the object to deserialize to
     * @return The decrypted and deserialized object
     * @throws CookieDecryptionException If decryption or deserialization fails
     */
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

    /**
     * Decrypts a JWE string to its original plaintext.
     * 
     * @param jwe The JWE string to decrypt
     * @return The decrypted plaintext
     * @throws CookieDecryptionException If decryption fails
     */
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

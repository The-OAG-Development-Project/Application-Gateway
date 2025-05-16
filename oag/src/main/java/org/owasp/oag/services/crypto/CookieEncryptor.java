package org.owasp.oag.services.crypto;

import org.owasp.oag.exception.CookieDecryptionException;

/**
 * Interface for encrypting and decrypting cookies.
 */
public interface CookieEncryptor {
    /**
     * Encrypts an object.
     *
     * @param payload The object to encrypt.
     * @return The encrypted object as a string.
     */
    String encryptObject(Object payload);

    /**
     * Decrypts a JWE string to an object of the specified class.
     *
     * @param jwe   The JWE string to decrypt.
     * @param clazz The class of the object to decrypt to.
     * @param <T>   The type of the object to decrypt to.
     * @return The decrypted object.
     * @throws CookieDecryptionException if the cookie cannot be decrypted.
     */
    <T> T decryptObject(String jwe, Class<T> clazz) throws CookieDecryptionException;
}

package org.owasp.oag.services.crypto;

public interface CookieEncryptor {
    String encryptObject(Object payload);

    <T> T decryptObject(String jwe, Class<T> clazz) throws CookieDecryptionException;
}

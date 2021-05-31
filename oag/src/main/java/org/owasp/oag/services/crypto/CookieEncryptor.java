package org.owasp.oag.services.crypto;

import org.owasp.oag.exception.CookieDecryptionException;

public interface CookieEncryptor {
    String encryptObject(Object payload);

    <T> T decryptObject(String jwe, Class<T> clazz) throws CookieDecryptionException;
}

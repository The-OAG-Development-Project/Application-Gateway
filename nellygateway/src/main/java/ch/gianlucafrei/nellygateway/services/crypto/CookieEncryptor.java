package ch.gianlucafrei.nellygateway.services.crypto;

public interface CookieEncryptor {
    String encryptObject(Object payload);

    <T> T decryptObject(String jwe, Class<T> clazz);
}

package ch.gianlucafrei.nellygateway.services.crypto;

public class CookieDecryptionException extends Exception {

    public CookieDecryptionException(String message) {
        super(message);
    }

    public CookieDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}

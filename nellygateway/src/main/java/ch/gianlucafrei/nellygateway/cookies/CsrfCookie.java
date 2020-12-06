package ch.gianlucafrei.nellygateway.cookies;

public class CsrfCookie {

    public static final String NAME = "csrf";

    private String csrfToken;

    public CsrfCookie(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }
}

package ch.gianlucafrei.nellygateway.config.configuration;

public class SessionBehaviour {

    private int sessionDuration;
    private String redirectLoginSuccess;
    private String redirectLoginFailure;
    private String redirectLogout;

    public int getSessionDuration() {
        return sessionDuration;
    }

    public void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public String getRedirectLoginSuccess() {
        return redirectLoginSuccess;
    }

    public void setRedirectLoginSuccess(String redirectLoginSuccess) {
        this.redirectLoginSuccess = redirectLoginSuccess;
    }

    public String getRedirectLoginFailure() {
        return redirectLoginFailure;
    }

    public void setRedirectLoginFailure(String redirectLoginFailure) {
        this.redirectLoginFailure = redirectLoginFailure;
    }

    public String getRedirectLogout() {
        return redirectLogout;
    }

    public void setRedirectLogout(String redirectLogout) {
        this.redirectLogout = redirectLogout;
    }
}

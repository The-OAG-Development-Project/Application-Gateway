package ch.gianlucafrei.nellygateway.config.configuration;

public class SessionBehaviour {

    private int sessionDuration;
    private String redirectLoginSuccess;
    private String redirectLoginFailure;
    private String redirectLogout;

    public int getSessionDuration() {
        return sessionDuration;
    }

    private void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    public String getRedirectLoginSuccess() {
        return redirectLoginSuccess;
    }

    private void setRedirectLoginSuccess(String redirectLoginSuccess) {
        this.redirectLoginSuccess = redirectLoginSuccess;
    }

    public String getRedirectLoginFailure() {
        return redirectLoginFailure;
    }

    private void setRedirectLoginFailure(String redirectLoginFailure) {
        this.redirectLoginFailure = redirectLoginFailure;
    }

    public String getRedirectLogout() {
        return redirectLogout;
    }

    private void setRedirectLogout(String redirectLogout) {
        this.redirectLogout = redirectLogout;
    }
}

package ch.gianlucafrei.nellygateway.config.configuration;

import ch.gianlucafrei.nellygateway.config.ErrorValidation;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class SessionBehaviour implements ErrorValidation {

    private int sessionDuration;
    private int renewWhenLessThan;
    private String redirectLoginSuccess;
    private String redirectLoginFailure;
    private String redirectLogout;

    public SessionBehaviour() {
    }

    public SessionBehaviour(int sessionDuration, int renewWhenLessThan, String redirectLoginSuccess, String redirectLoginFailure, String redirectLogout) {
        this.sessionDuration = sessionDuration;
        this.renewWhenLessThan = renewWhenLessThan;
        this.redirectLoginSuccess = redirectLoginSuccess;
        this.redirectLoginFailure = redirectLoginFailure;
        this.redirectLogout = redirectLogout;
    }

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

    public int getRenewWhenLessThan() {
        return renewWhenLessThan;
    }

    private void setRenewWhenLessThan(int renewWhenLessThan) {
        this.renewWhenLessThan = renewWhenLessThan;
    }

    @Override
    public List<String> getErrors(ApplicationContext context) {
        var errors = new ArrayList<String>();

        if (sessionDuration < 60)
            errors.add("session duration is to short < 60s");

        if (redirectLoginSuccess == null)
            errors.add("redirectLoginSuccess not defined");

        if (redirectLoginFailure == null)
            errors.add("redirectLoginFailure not defined");

        if (redirectLogout == null)
            errors.add("redirectLogout not defined");

        if(! errors.isEmpty())
            return errors;

        if (renewWhenLessThan >= sessionDuration)
            errors.add("renewWhenLessThan cannot be >= than sessionDuration");

        return errors;
    }
}

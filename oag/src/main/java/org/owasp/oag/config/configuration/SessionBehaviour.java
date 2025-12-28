package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures session behavior settings, including duration, renewal policy, and redirect URLs.
 * This class defines how sessions are managed and handles redirects for various authentication states.
 */
public class SessionBehaviour implements ErrorValidation {

    /** Duration of a session in seconds */
    private int sessionDuration;
    
    /** Threshold in seconds below which a session should be renewed */
    private int renewWhenLessThan;
    
    /** URL to redirect to after a successful login */
    private String redirectLoginSuccess;
    
    /** URL to redirect to after a failed login */
    private String redirectLoginFailure;
    
    /** URL to redirect to after logout */
    private String redirectLogout;

    /**
     * Default constructor for session behavior.
     */
    public SessionBehaviour() {
    }

    /**
     * Creates a fully configured session behavior instance.
     *
     * @param sessionDuration The duration of the session in seconds
     * @param renewWhenLessThan The threshold in seconds below which a session should be renewed
     * @param redirectLoginSuccess The URL to redirect to after a successful login
     * @param redirectLoginFailure The URL to redirect to after a failed login
     * @param redirectLogout The URL to redirect to after logout
     */
    public SessionBehaviour(int sessionDuration, int renewWhenLessThan, String redirectLoginSuccess, String redirectLoginFailure, String redirectLogout) {
        this.sessionDuration = sessionDuration;
        this.renewWhenLessThan = renewWhenLessThan;
        this.redirectLoginSuccess = redirectLoginSuccess;
        this.redirectLoginFailure = redirectLoginFailure;
        this.redirectLogout = redirectLogout;
    }

    /**
     * Gets the configured session duration in seconds.
     *
     * @return The session duration in seconds
     */
    public int getSessionDuration() {
        return sessionDuration;
    }

    /**
     * Sets the session duration in seconds.
     *
     * @param sessionDuration The session duration in seconds
     */
    private void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    /**
     * Gets the URL to redirect to after a successful login.
     *
     * @return The successful login redirect URL
     */
    public String getRedirectLoginSuccess() {
        return redirectLoginSuccess;
    }

    /**
     * Sets the URL to redirect to after a successful login.
     *
     * @param redirectLoginSuccess The successful login redirect URL
     */
    private void setRedirectLoginSuccess(String redirectLoginSuccess) {
        this.redirectLoginSuccess = redirectLoginSuccess;
    }

    /**
     * Gets the URL to redirect to after a failed login.
     *
     * @return The failed login redirect URL
     */
    public String getRedirectLoginFailure() {
        return redirectLoginFailure;
    }

    /**
     * Sets the URL to redirect to after a failed login.
     *
     * @param redirectLoginFailure The failed login redirect URL
     */
    private void setRedirectLoginFailure(String redirectLoginFailure) {
        this.redirectLoginFailure = redirectLoginFailure;
    }

    /**
     * Gets the URL to redirect to after logout.
     *
     * @return The logout redirect URL
     */
    public String getRedirectLogout() {
        return redirectLogout;
    }

    /**
     * Sets the URL to redirect to after logout.
     *
     * @param redirectLogout The logout redirect URL
     */
    private void setRedirectLogout(String redirectLogout) {
        this.redirectLogout = redirectLogout;
    }

    /**
     * Gets the threshold in seconds below which a session should be renewed.
     *
     * @return The renewal threshold in seconds
     */
    public int getRenewWhenLessThan() {
        return renewWhenLessThan;
    }

    /**
     * Sets the threshold in seconds below which a session should be renewed.
     *
     * @param renewWhenLessThan The renewal threshold in seconds
     */
    private void setRenewWhenLessThan(int renewWhenLessThan) {
        this.renewWhenLessThan = renewWhenLessThan;
    }

    /**
     * Validates the session behavior configuration and returns any errors.
     *
     * @param context The application context
     * @return A list of validation error messages, empty if no errors are found
     */
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

        if (!errors.isEmpty())
            return errors;

        if (renewWhenLessThan >= sessionDuration)
            errors.add("renewWhenLessThan cannot be >= than sessionDuration");

        return errors;
    }
}

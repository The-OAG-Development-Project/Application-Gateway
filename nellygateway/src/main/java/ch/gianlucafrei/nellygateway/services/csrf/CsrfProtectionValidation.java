package ch.gianlucafrei.nellygateway.services.csrf;

import javax.servlet.http.HttpServletRequest;

public interface CsrfProtectionValidation {

    boolean shouldBlockRequest(HttpServletRequest request);
}

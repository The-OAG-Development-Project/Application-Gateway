package ch.gianlucafrei.nellygateway.services.csrf;

import javax.servlet.http.HttpServletRequest;

public interface CsrfProtectionCheck {

    boolean shouldBlockRequest(HttpServletRequest request);
}

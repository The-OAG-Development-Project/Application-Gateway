package ch.gianlucafrei.nellygateway.services.csrf;

import java.net.http.HttpRequest;

public interface CSRFProtection {

    boolean shouldBlockRequest(HttpRequest request);
}

package org.owasp.oag.services.csrf;

import org.springframework.context.ApplicationContext;
import org.springframework.web.server.ServerWebExchange;

public interface CsrfProtectionValidation {

    static CsrfProtectionValidation loadValidationImplementation(String csrfProtectionMethod, ApplicationContext context) {

        String beanname = "csrf-" + csrfProtectionMethod + "-validation";
        CsrfProtectionValidation validationImplementation = context.getBean(beanname, CsrfProtectionValidation.class);

        if (validationImplementation == null) {
            throw new RuntimeException("csrf validation implementation not found: " + beanname);
        }

        return validationImplementation;
    }

    boolean needsRequestBody();

    boolean shouldBlockRequest(ServerWebExchange exchange, String requestBody);
}

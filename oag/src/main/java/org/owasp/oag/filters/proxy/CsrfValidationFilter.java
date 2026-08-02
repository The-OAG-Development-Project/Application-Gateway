package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Filter that validates CSRF (Cross-Site Request Forgery) protection for non-safe HTTP methods.
 * This filter checks if the current request method is considered "safe" according to the security profile.
 * If the method is not safe, it applies the configured CSRF protection validation.
 * Requests that fail CSRF validation are blocked with a 401 Unauthorized response.
 * This filter only handles CSRF validation that doesn't require the request body.
 * For CSRF validation that requires the request body, the request is passed to CsrfValidationFilterWithBody.
 */
@Order(30)
@Component
public class CsrfValidationFilter extends RouteAwareFilter {

    /**
     * Logger for this class.
     */
    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilter.class);

    /**
     * Factory for creating CSRF validation implementations.
     * Used to load the appropriate CSRF validation strategy based on the security profile.
     */
    @Autowired
    private CsrfValidationImplementationFactory csrfValidationImplementationFactory;

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        log.trace("Execute CsrfValidationFilter.");
        var securityProfile = routeContext.getSecurityProfile();

        HttpMethod reqMethod = request.method();
        boolean isSafeMethod = securityProfile.getCsrfSafeMethods().contains(reqMethod.name());

        if (!isSafeMethod) {

            String csrfProtectionMethod = securityProfile.getCsrfProtection();
            CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

            if (csrfValidation.needsRequestBody())
                return next.handle(request); // will be done by CsrfValidationFilterWithBody instead

            if (csrfValidation.shouldBlockRequest(request.servletRequest(), null)) {

                log.info("Blocked request due to csrf protection, route={}, reqMethod={}, csrfMethod={}", routeContext.getRouteName(), reqMethod, csrfProtectionMethod);

                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        return next.handle(request);
    }
}

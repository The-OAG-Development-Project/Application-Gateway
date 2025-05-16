package org.owasp.oag.filters.proxy;

import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

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
     * Main configuration of the application.
     * Used to access security profiles and CSRF settings.
     */
    @Autowired
    private MainConfig config;

    /**
     * Factory for creating CSRF validation implementations.
     * Used to load the appropriate CSRF validation strategy based on the security profile.
     */
    @Autowired
    private CsrfValidationImplementationFactory csrfValidationImplementationFactory;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute CsrfValidationFilter");
        var securityProfile = routeContext.getSecurityProfile();

        // Load security profile
        HttpMethod reqMethod = exchange.getRequest().getMethod();
        boolean isSafeMethod = securityProfile.getCsrfSafeMethods().contains(reqMethod.name());

        if (!isSafeMethod) {

            String csrfProtectionMethod = securityProfile.getCsrfProtection();
            CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

            if (csrfValidation.needsRequestBody())
                return chain.filter(exchange); // will be done by CsrfValidationFilterWithBody instead

            boolean shouldBlock = csrfValidation.shouldBlockRequest(exchange, null);

            if (shouldBlock) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                logInfo(log, exchange,"Blocked request due to csrf protection, route={}, reqMethod={}, csrfMethod={}",
                        routeContext.getRouteName(),
                        reqMethod,
                        csrfProtectionMethod);

                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }
}

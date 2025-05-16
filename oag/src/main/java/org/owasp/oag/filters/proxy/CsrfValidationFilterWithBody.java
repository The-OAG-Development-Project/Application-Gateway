package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.filters.spring.ExtractAuthenticationFilter;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * Filter that validates CSRF tokens for requests that require examining the request body.
 * This filter extends ReadRequestBodyFilter to access the request body when needed for CSRF validation.
 * It only processes unsafe HTTP methods (non-GET/HEAD) for authenticated sessions and
 * when the configured CSRF protection method requires body content for validation.
 * Requests that fail CSRF validation are blocked with a 401 Unauthorized response.
 */
@Order(31)
@Component
public class CsrfValidationFilterWithBody extends ReadRequestBodyFilter {

    /** Attribute name for the request body in the exchange. */
    public static final String REQUEST_BODY_ATTRIBUTE = "RequestBody";
    
    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilterWithBody.class);

    /** Factory for creating the appropriate CSRF validation implementation based on configuration. */
    @Autowired
    private CsrfValidationImplementationFactory csrfValidationImplementationFactory;

    /**
     * Determines whether this filter should run for the current request.
     * The filter runs only when:
     * 1. The HTTP method is not in the list of CSRF safe methods
     * 2. The user has an active session
     * 3. The configured CSRF protection method requires examining the request body
     *
     * @param exchange The current server exchange
     * @param routeContext The gateway route context
     * @return true if the filter should process this request, false otherwise
     */
    @Override
    protected boolean shouldRun(ServerWebExchange exchange, GatewayRouteContext routeContext) {

        HttpMethod reqMethod = exchange.getRequest().getMethod();
        boolean isSafeMethod = routeContext.getSecurityProfile().getCsrfSafeMethods().contains(reqMethod.name());

        if (isSafeMethod)
            return false;

        // Dont do the validation if there is no user session
        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);
        //noinspection ConstantConditions
        if ( sessionOptional.isEmpty())
            return false;

        // Only execute if body is needed for csrf validation, otherwise validation is done by CsrfValidationFilter
        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

        return csrfValidation.needsRequestBody();
    }

    /**
     * Processes the request body and performs CSRF validation.
     * If validation fails, the request is blocked with a 401 Unauthorized response.
     * An empty string is used if the body is null.
     *
     * @param exchange The current server exchange
     * @param body The request body content, may be null
     * @param routeContext The gateway route context
     * @throws ResponseStatusException with HTTP 401 if CSRF validation fails
     */
    @Override
    protected void consumeBody(ServerWebExchange exchange, String body, GatewayRouteContext routeContext) {

        logTrace(log, exchange, "Execute ExtractAuthenticationFilterWithBody");

        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

        // In the case that we have a post request but no body
        body = body == null ? "" : body;

        boolean shouldBlock = csrfValidation.shouldBlockRequest(exchange, body);

        if (shouldBlock) {

            logInfo(log, exchange,"Blocked request due to csrf protection, route={}, reqMethod={}, csrfMethod={}",
                    routeContext.getRouteName(),
                    exchange.getRequest().getMethod(),
                    csrfProtectionMethod);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}

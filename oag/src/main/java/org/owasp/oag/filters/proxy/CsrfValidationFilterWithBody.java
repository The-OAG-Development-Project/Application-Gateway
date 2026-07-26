package org.owasp.oag.filters.proxy;

import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.filters.servlet.ExtractAuthenticationFilter;
import org.owasp.oag.infrastructure.factories.CsrfValidationImplementationFactory;
import org.owasp.oag.services.csrf.CsrfProtectionValidation;
import org.owasp.oag.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * Filter that validates CSRF tokens for requests that require examining the request body.
 * It only processes unsafe HTTP methods (non-GET/HEAD) for authenticated sessions and
 * when the configured CSRF protection method requires body content for validation.
 * Requests that fail CSRF validation are blocked with a 401 Unauthorized response.
 * <p>
 * The body is read through the gateway's request body cache, so the bytes are still available for the
 * downstream call after this filter inspected them.
 */
@Order(31)
@Component
public class CsrfValidationFilterWithBody extends RouteAwareFilter {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilterWithBody.class);

    /** Factory for creating the appropriate CSRF validation implementation based on configuration. */
    @Autowired
    private CsrfValidationImplementationFactory csrfValidationImplementationFactory;

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        if (!shouldRun(request, routeContext))
            return next.handle(request);

        logTrace(log, request.servletRequest(), "Execute CsrfValidationFilterWithBody");

        var contentLength = request.headers().firstHeader(HttpHeaders.CONTENT_LENGTH);
        String body = "";
        if (contentLength != null) {
            var cachedBody = MvcUtils.getOrCacheBody(request);
            body = new String(cachedBody.readAllBytes(), StandardCharsets.UTF_8);
            // readAllBytes() left the cached stream at its end, rewind it so the downstream call
            // still sees the whole body.
            cachedBody.reset();
        }

        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

        if (csrfValidation.shouldBlockRequest(request.servletRequest(), body)) {

            logInfo(log, request.servletRequest(),
                    "Blocked request due to csrf protection, route={}, reqMethod={}, csrfMethod={}",
                    routeContext.getRouteName(), request.method(), csrfProtectionMethod);

            return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
        }

        return next.handle(BeforeFilterFunctions.adaptCachedBody().apply(request));
    }

    /**
     * Determines whether this filter should run for the current request.
     * The filter runs only when:
     * 1. The HTTP method is not in the list of CSRF safe methods
     * 2. The user has an active session
     * 3. The configured CSRF protection method requires examining the request body
     *
     * @param request The current request
     * @param routeContext The gateway route context
     * @return true if the filter should process this request, false otherwise
     */
    protected boolean shouldRun(ServerRequest request, GatewayRouteContext routeContext) {

        HttpMethod reqMethod = request.method();
        boolean isSafeMethod = routeContext.getSecurityProfile().getCsrfSafeMethods().contains(reqMethod.name());

        if (isSafeMethod)
            return false;

        // Dont do the validation if there is no user session
        Optional<Session> sessionOptional = ExtractAuthenticationFilter.extractSessionFromRequest(request.servletRequest());
        if (sessionOptional.isEmpty())
            return false;

        // Only execute if body is needed for csrf validation, otherwise validation is done by CsrfValidationFilter
        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

        return csrfValidation.needsRequestBody();
    }
}

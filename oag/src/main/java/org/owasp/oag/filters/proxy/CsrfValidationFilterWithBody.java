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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

import static org.owasp.oag.utils.LoggingUtils.logInfo;
import static org.owasp.oag.utils.LoggingUtils.logTrace;

@Order(31)
@Component
public class CsrfValidationFilterWithBody extends ReadRequestBodyFilter {

    public static final String REQUEST_BODY_ATTRIBUTE = "RequestBody";
    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilterWithBody.class);

    @Autowired
    private CsrfValidationImplementationFactory csrfValidationImplementationFactory;

    @Override
    protected boolean shouldRun(ServerWebExchange exchange, GatewayRouteContext routeContext) {

        String reqMethod = exchange.getRequest().getMethodValue();
        boolean isSafeMethod = routeContext.getSecurityProfile().getCsrfSafeMethods()
                .contains(reqMethod);

        if (isSafeMethod)
            return false;

        // Dont do the validation if there is no user session
        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.OAG_SESSION);
        if (sessionOptional.isEmpty())
            return false;

        // Only execute if body is needed for csrf validation, otherwise validation is done by CsrfValidationFilter
        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = csrfValidationImplementationFactory.loadCsrfValidationImplementation(csrfProtectionMethod);

        return csrfValidation.needsRequestBody();
    }

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
                    exchange.getRequest().getMethodValue(),
                    csrfProtectionMethod);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}

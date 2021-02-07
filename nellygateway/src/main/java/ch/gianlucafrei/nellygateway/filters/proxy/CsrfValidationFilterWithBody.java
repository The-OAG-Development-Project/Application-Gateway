package ch.gianlucafrei.nellygateway.filters.proxy;

import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfProtectionValidation;
import ch.gianlucafrei.nellygateway.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Component
public class CsrfValidationFilterWithBody extends ReadRequestBodyFilter {

    public static final String REQUEST_BODY_ATTRIBUTE = "RequestBody";
    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilterWithBody.class);
    @Autowired
    private ApplicationContext context;

    @Override
    protected boolean shouldRun(ServerWebExchange exchange, GatewayRouteContext routeContext) {

        String reqMethod = exchange.getRequest().getMethodValue();
        boolean isSafeMethod = routeContext.getSecurityProfile().getCsrfSafeMethods()
                .contains(reqMethod);

        if (isSafeMethod)
            return false;

        // Dont do the validation if there is no user session
        Optional<Session> sessionOptional = exchange.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);
        if (sessionOptional.isEmpty())
            return false;

        // Only execute if body is needed for csrf validation, otherwise validation is done by CsrfValidationFilter
        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = CsrfProtectionValidation.loadValidationImplementation(csrfProtectionMethod, context);

        return csrfValidation.needsRequestBody();
    }

    @Override
    protected void consumeBody(ServerWebExchange exchange, String body, GatewayRouteContext routeContext) {

        String csrfProtectionMethod = routeContext.getSecurityProfile().getCsrfProtection();
        CsrfProtectionValidation csrfValidation = CsrfProtectionValidation.loadValidationImplementation(csrfProtectionMethod, context);

        // In the case that we have a post request but no body
        body = body == null ? "" : body;

        boolean shouldBlock = csrfValidation.shouldBlockRequest(exchange, body);

        if (shouldBlock) {

            log.info("Blocked request due to csrf protection, route={}, reqMethod={}, csrfMethod={}",
                    routeContext.getRouteName(),
                    exchange.getRequest().getMethodValue(),
                    csrfProtectionMethod);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}

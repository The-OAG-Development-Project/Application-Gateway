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

@Order(30)
@Component
public class CsrfValidationFilter extends RouteAwareFilter {

    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilter.class);

    @Autowired
    private MainConfig config;

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

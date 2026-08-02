package org.owasp.oag.filters.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.owasp.oag.controllers.dto.SessionInformation;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.cookies.LoginCookie;
import org.owasp.oag.cookies.LoginStateCookie;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.filters.servlet.TraceContextFilter;
import org.owasp.oag.logging.TraceContext;
import org.owasp.oag.logging.TraceHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This gateway filter adds additional headers to the request
 */
@Order(41)
@Component
public class DownstreamHeaderFilter extends RouteAwareFilter {

    /**
     * Header name for identifying the proxy
     */
    public static final String X_PROXY = "X-PROXY";

    /**
     * Header name for indicating the authentication status
     */
    public static final String X_OAG_STATUS = "X-OAG-Status";

    /**
     * Value for the X-PROXY header that identifies this application gateway
     */
    public static final String X_PROXY_VALUE = "OWASP Application Gateway";

    private static final Logger log = LoggerFactory.getLogger(DownstreamHeaderFilter.class);

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                 GatewayRouteContext routeContext) throws Exception {

        log.trace("Execute DownstreamHeaderFilter.");

        var builder = ServerRequest.from(request);

        builder = addOagHeaders(routeContext, builder);
        builder = addTraceHeaders(request.servletRequest(), builder);
        builder = filterRequestCookies(request, builder);

        return next.handle(builder.build());
    }

    /**
     * Adds OAG-specific headers to the request.
     *
     * @param routeContext The gateway route context containing session information
     * @param builder The request builder to add headers to
     * @return The updated request builder with added headers
     */
    protected ServerRequest.Builder addOagHeaders(GatewayRouteContext routeContext, ServerRequest.Builder builder) {

        // Add OAG status headers
        var status = routeContext.getSessionOptional().isPresent()
                ? SessionInformation.SESSION_STATE_AUTHENTICATED
                : SessionInformation.SESSION_STATE_ANONYMOUS;

        return builder.headers(headers -> {
            headers.set(X_PROXY, X_PROXY_VALUE);
            headers.set(X_OAG_STATUS, status);
        });
    }

    /**
     * Adds the tracing headers decided for this request to the downstream request.
     *
     * @param request The current request holding the trace headers attribute
     * @param builder The request builder to add headers to
     * @return The updated request builder
     */
    protected ServerRequest.Builder addTraceHeaders(HttpServletRequest request, ServerRequest.Builder builder) {

        TraceHeaders traceHeaders = TraceContext.REQUEST_TRACE_HEADERS.get();
        if (traceHeaders == null)
            return builder;

        if (traceHeaders.downstreamHeaders().isEmpty())
            return builder;

        builder = builder.headers(headers -> traceHeaders.downstreamHeaders().forEach(headers::set));

        log.debug("Added trace id to downstream call.");
        return builder;
    }

    /**
     * Removes OAG's own cookies from the request forwarded downstream.
     *
     * @param request The current request
     * @param builder The request builder to update
     * @return The updated request builder
     */
    protected ServerRequest.Builder filterRequestCookies(ServerRequest request, ServerRequest.Builder builder) {

        // Remove OAG cookies
        var cookieBlacklist = Set.of(LoginCookie.NAME, CsrfCookie.NAME, LoginStateCookie.NAME);

        // Build the header value from name and value: jakarta.servlet.http.Cookie#toString does not
        // render a Cookie header, it renders the java object identity.
        var cookieHeaderValue = request.cookies().entrySet().stream()
                .filter(e -> !cookieBlacklist.contains(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; "));

        return builder.headers(headers -> headers.set(HttpHeaders.COOKIE, cookieHeaderValue));
    }
}

package org.owasp.oag.filters.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Logs the reason of a request that failed with a {@link ResponseStatusException}.
 * <p>
 * This has to be a {@code HandlerExceptionResolver} rather than part of a servlet filter: the
 * DispatcherServlet resolves a ResponseStatusException into a status code and never rethrows it, so
 * the exception is not observable from a filter. Returning null leaves the response entirely to the
 * standard resolvers - this participates only to observe.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestFailureLogger implements HandlerExceptionResolver {

    /**
     * Request attribute set once this logger emitted the response line for a request. {@link SimpleLogFilter}
     * reads it and then skips its own line, so a failed request is logged exactly once.
     */
    public static final String RESPONSE_LOGGED_ATTRIBUTE = "oag.responseLogged";

    private static final Logger log = LoggerFactory.getLogger(RequestFailureLogger.class);

    @Override
    public @Nullable ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                                   @Nullable Object handler, Exception ex) {

        if (ex instanceof ResponseStatusException responseStatusException) {
            log.info("Response status code {} for {} {} errorReason: '{}'",
                    responseStatusException.getStatusCode(), request.getMethod(), request.getRequestURL(),
                    responseStatusException.getReason());
            request.setAttribute(RESPONSE_LOGGED_ATTRIBUTE, Boolean.TRUE);
        }

        // Never resolve - the standard resolvers produce the response.
        return null;
    }
}

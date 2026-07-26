package org.owasp.oag.filters.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.owasp.oag.utils.LoggingUtils;
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

    private static final Logger log = LoggerFactory.getLogger(RequestFailureLogger.class);

    @Override
    public @Nullable ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                                   @Nullable Object handler, Exception ex) {

        if (ex instanceof ResponseStatusException responseStatusException) {
            LoggingUtils.logInfo(log, request, "Response status code {} for {} {} errorReason: '{}'",
                    responseStatusException.getStatusCode(), request.getMethod(), request.getRequestURL(),
                    responseStatusException.getReason());
        }

        // Never resolve - the standard resolvers produce the response.
        return null;
    }
}

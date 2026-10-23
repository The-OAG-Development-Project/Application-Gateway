package org.owasp.oag.filters.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that logs HTTP request and response information.
 * Logs the request method, URI, and response status code for all requests.
 * <p>
 * The reason of a request that failed with a {@code ResponseStatusException} is logged by
 * {@link RequestFailureLogger} instead, because the DispatcherServlet resolves such an exception into a
 * status code and never rethrows it, so it cannot be observed from a servlet filter. That logger already
 * emits the response line including the reason, so this filter stays silent for such a request to keep
 * exactly one response line per request.
 */
@Order(20)
@Component
public class SimpleLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        log.trace("Execute SimpleLogFilter.");
        log.debug("Request to {} {}", request.getMethod(), request.getRequestURL());

        chain.doFilter(request, response);

        if (request.getAttribute(RequestFailureLogger.RESPONSE_LOGGED_ATTRIBUTE) != null)
            return;

        log.info("Response status code {} for {} {}",
                renderStatus(response.getStatus()), request.getMethod(), request.getRequestURL());
    }

    /**
     * Renders a status code as {@code "401 UNAUTHORIZED"} for the codes the framework knows and as the
     * bare number otherwise.
     *
     * @param status the numeric status code of the response
     * @return the value to log for that status code
     */
    private static Object renderStatus(int status) {

        try {
            return HttpStatusCode.valueOf(status);
        } catch (IllegalArgumentException e) {
            // valueOf rejects codes outside 100..999, which a container should never produce.
            return status;
        }
    }
}

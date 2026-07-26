package org.owasp.oag.filters.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that logs HTTP request and response information.
 * Logs the request method, URI, and response status code for all requests.
 * <p>
 * The reason of a request that failed with a {@code ResponseStatusException} is logged by
 * {@link RequestFailureLogger} instead, because the DispatcherServlet resolves such an exception into a
 * status code and never rethrows it, so it cannot be observed from a servlet filter.
 */
@Order(20)
@Component
public class SimpleLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        LoggingUtils.logTrace(log, request, "Execute SimpleLogFilter");
        LoggingUtils.logDebug(log, request, "Request to {} {}", request.getMethod(), request.getRequestURL());

        chain.doFilter(request, response);

        LoggingUtils.logInfo(log, request, "Response status code {} for {} {}",
                response.getStatus(), request.getMethod(), request.getRequestURL());
    }
}

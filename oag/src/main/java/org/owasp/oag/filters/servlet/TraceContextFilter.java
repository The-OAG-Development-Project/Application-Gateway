package org.owasp.oag.filters.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.owasp.oag.logging.TraceContextBridge;
import org.owasp.oag.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Responsible to setup a trace id based on the configured trace mechanism.
 * <p>
 * The trace id is put into the request attributes and into the MDC, so every log statement written while
 * handling the request carries it.
 */
@Order(-10)
@Component
public class TraceContextFilter extends OncePerRequestFilter {

    /**
     * The key where the trace ID of the current request is stored, both in the request attributes and in
     * the MDC (Mapped Diagnostic Context) used for logging.
     */
    public static final String TRACE_ID_CONTEXT_KEY = "oag.CorrId";

    /**
     * The key where the {@link org.owasp.oag.logging.TraceHeaders} of the current request is stored in the
     * request attributes.
     */
    public static final String TRACE_HEADERS_ATTRIBUTE = "oag.TraceHeaders";

    private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);

    /**
     * Bridge to the configured trace context implementation.
     */
    @Autowired
    TraceContextBridge traceContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        var mainHeaderName = traceContext.getMainRequestHeader();
        var secondaryHeaderName = traceContext.getSecondaryRequestHeader();
        var traceHeaders = traceContext.createTraceHeaders(
                mainHeaderName == null ? null : request.getHeader(mainHeaderName),
                secondaryHeaderName == null ? null : request.getHeader(secondaryHeaderName));

        request.setAttribute(TRACE_ID_CONTEXT_KEY, traceHeaders.traceId());
        request.setAttribute(TRACE_HEADERS_ATTRIBUTE, traceHeaders);
        traceHeaders.responseHeaders().forEach(response::addHeader);

        try (MDC.MDCCloseable cMdc = MDC.putCloseable(TRACE_ID_CONTEXT_KEY, traceHeaders.traceId())) {

            LoggingUtils.logDebug(log, request, "Tracing started.");

            if (!traceHeaders.responseHeaders().isEmpty()) {
                LoggingUtils.logDebug(log, request, "Adding trace id to upstream response");
            }

            log.debug("Generated new request id.");
            chain.doFilter(request, response);
        }
    }
}

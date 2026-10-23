package org.owasp.oag.filters.servlet;

import ch.qos.logback.classic.scoped.ScopedMDC;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.owasp.oag.exception.ApplicationException;
import org.owasp.oag.logging.TraceContextBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

import static org.owasp.oag.logging.TraceContext.REQUEST_TRACE_HEADERS;

/**
 * Responsible to setup a trace id based on the configured trace mechanism.
 * <p>
 * The trace id is put into the request attributes and into the MDC, so every log statement written while
 * handling the request carries it.
 * This should be the first filter in the chain.
 */
@Order(-10)
@Component
public class TraceContextFilter extends OncePerRequestFilter {

    /**
     * The key where the trace ID of the current request is stored, both in the request attributes and in
     * the MDC (Mapped Diagnostic Context) used for logging.
     */
    public static final String TRACE_ID_CONTEXT_KEY = "oag.CorrId";

    private static final Logger log = LoggerFactory.getLogger(TraceContextFilter.class);

    /**
     * Bridge to the configured trace context implementation.
     */
    @Autowired
    TraceContextBridge traceContext;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) {

        var mainHeaderName = traceContext.getMainRequestHeader();
        var secondaryHeaderName = traceContext.getSecondaryRequestHeader();
        var traceHeaders = traceContext.createTraceHeaders(
                mainHeaderName == null ? null : request.getHeader(mainHeaderName),
                secondaryHeaderName == null ? null : request.getHeader(secondaryHeaderName));

        Map<String, String> logContextMap = Map.of(
          TRACE_ID_CONTEXT_KEY, traceHeaders.traceId()
        );

        ScopedValue.where(REQUEST_TRACE_HEADERS, traceHeaders).run(() ->
            ScopedMDC.putAll(logContextMap).run( () -> {
                log.debug("Tracing started.");

                if (!traceHeaders.responseHeaders().isEmpty()) {
                    traceHeaders.responseHeaders().forEach(response::addHeader);
                    log.debug("Adding trace id to upstream response.");
                }

                log.debug("Generated new request id.");
              try {
                chain.doFilter(request, response);
              } catch (IOException | ServletException e) {
                  throw new ApplicationException("General Exception during request.", e);
              }
            })
        );
    }
}

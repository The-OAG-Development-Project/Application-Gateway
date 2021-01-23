package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.logging.W3cTraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This Filter adds a correlation-id (trace id) to each request, so that log correlation is greatly simplified.
 * The correlation-id is per default in format specified by the W3C Trace Context specification
 * (see https://w3c.github.io/trace-context/).
 * It support the "traceparent" header and does not introduce a vendor specific "tracestate" but supports passing this information on when required.
 * It also supports returning the traceresponse header
 * The traceparent header looks something like this:
 * traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01
 * its format is:
 * Version-TraceId-ParentId-Flag
 * <p>
 * As OAGW is a boundary service protecting entry into a secured network, its default is to restart the trace and never forward
 * trace-information that was received from a caller.
 * This behaviour can be disabled.
 * <p>
 * Example configuration (showing default values):
 * <code>
 * ADD config here:
 * Flags:
 * forwardIncomingTrace (false -> creates new traceparent),
 * acceptTraceState (false -> does not forward/accept tracestate header, only relevant when forwardIncomingTrace is true)
 * returnTraceResponse (true -> returns traceresponse header)
 * Setting:
 * maxSizeTraceState (1024 characters, only relevant when forwardIncomingTrace=true)
 *
 * </code>
 */
@Order(10)
@Component
public class W3cTraceContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(W3cTraceContextFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        W3cTraceContext traceCtx = W3cTraceContext.establishNew();
        try {
            log.debug("Established new correlation id. It might change.");
            if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
                log.info("Non-Http request, correlation-id can not be taken over or returned to caller");
                return;
            }

            HttpServletRequest req = (HttpServletRequest) request;

            if (traceCtx.forwardIncomingTrace()) {
                // make sure we take over the passed in traceparent when it is valid
// TODO: implement
                if (traceCtx.acceptAdditionalTraceInfo()) {
                    // make sure we take over the trace state if it is valid
// TODO: implement
                }
            }

            chain.doFilter(request, response);

            HttpServletResponse res = (HttpServletResponse) response;
            if (traceCtx.shouldSendResponse()) {
                ((HttpServletResponse) response).setHeader(traceCtx.getResponseHeader(), traceCtx.getTraceString());
            }

            log.info("Response status code {} for {} {}", res.getStatus(), req.getMethod(), req.getRequestURI());
        } finally {
            if (traceCtx != null) {
                traceCtx.teardown();
            }
        }

    }
}

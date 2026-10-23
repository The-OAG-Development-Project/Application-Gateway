package org.owasp.oag.filters.servlet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the response line of the request log: the status is rendered with its reason phrase, and a request
 * whose failure reason {@link RequestFailureLogger} already logged is not logged a second time.
 */
class SimpleLogFilterTest {

    private final SimpleLogFilter filter = new SimpleLogFilter();
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger logger;
    private Level originalLevel;

    @BeforeEach
    void attachAppender() {

        logger = (Logger) LoggerFactory.getLogger(SimpleLogFilter.class);
        originalLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {

        logger.detachAppender(appender);
        appender.stop();
        logger.setLevel(originalLevel);
    }

    @Test
    void rendersTheStatusCodeWithItsReasonPhrase() throws Exception {

        var request = new MockHttpServletRequest("GET", "/foo");
        var response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(1, appender.list.size(), "expected exactly one log event");
        var message = appender.list.getFirst().getFormattedMessage();
        assertTrue(message.contains("Response status code 401 UNAUTHORIZED for GET"),
                "the status code must be logged with its reason phrase, got: " + message);
    }

    @Test
    void rendersAStatusCodeTheFrameworkDoesNotKnowAsItsNumber() throws Exception {

        var request = new MockHttpServletRequest("GET", "/foo");
        var response = new MockHttpServletResponse();
        response.setStatus(599);

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(1, appender.list.size(), "expected exactly one log event");
        var message = appender.list.getFirst().getFormattedMessage();
        assertTrue(message.contains("Response status code 599 for GET"),
                "an unknown status code must still be logged, got: " + message);
    }

    @Test
    void logsNothingWhenTheFailureReasonWasAlreadyLogged() throws Exception {

        var request = new MockHttpServletRequest("GET", "/foo");
        var response = new MockHttpServletResponse();

        // The dispatcher servlet runs inside the filter chain, so the exception resolver logs the response
        // line before this filter reaches its own logging statement.
        FilterChain dispatcher = (req, res) -> {
            ((HttpServletResponse) res).setStatus(HttpStatus.BAD_REQUEST.value());
            new RequestFailureLogger().resolveException((HttpServletRequest) req, (HttpServletResponse) res, null,
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url"));
        };

        filter.doFilter(request, response, dispatcher);

        assertTrue(appender.list.isEmpty(),
                "a failed request must be logged once, by RequestFailureLogger, not twice: " + appender.list);
    }
}

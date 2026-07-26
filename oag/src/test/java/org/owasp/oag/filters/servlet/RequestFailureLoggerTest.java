package org.owasp.oag.filters.servlet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the log statement that carries the reason of a failed request. Nothing else in the suite asserts
 * on it, so without this test the detail could disappear unnoticed.
 */
class RequestFailureLoggerTest {

    private final RequestFailureLogger requestFailureLogger = new RequestFailureLogger();
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger logger;
    private Level originalLevel;

    @BeforeEach
    void attachAppender() {

        logger = (Logger) LoggerFactory.getLogger(RequestFailureLogger.class);
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
    void logsTheReasonOfAResponseStatusExceptionWithoutResolvingIt() {

        var request = new MockHttpServletRequest("GET", "/auth/local/login");
        var exception = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid return url");

        var modelAndView = requestFailureLogger.resolveException(request, new MockHttpServletResponse(), null, exception);

        assertNull(modelAndView, "the logger must not resolve the exception, the standard resolvers do");
        assertEquals(1, appender.list.size(), "expected exactly one log event");
        assertTrue(appender.list.getFirst().getFormattedMessage().contains("errorReason: 'Invalid return url'"),
                "log message did not carry the error reason: " + appender.list.getFirst().getFormattedMessage());
    }

    @Test
    void logsNothingForOtherExceptions() {

        var request = new MockHttpServletRequest("GET", "/auth/local/login");

        var modelAndView = requestFailureLogger.resolveException(request, new MockHttpServletResponse(), null,
                new IllegalStateException("boom"));

        assertNull(modelAndView);
        assertTrue(appender.list.isEmpty(), "no log event expected for a non ResponseStatusException");
    }
}

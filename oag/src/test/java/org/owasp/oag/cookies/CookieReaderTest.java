package org.owasp.oag.cookies;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link CookieReader} finds a request cookie by its name.
 */
class CookieReaderTest {

    @Test
    void findsTheCookieWithTheRequestedName() {

        var request = requestWith(new Cookie("other", "x"), new Cookie("wanted", "value"));

        assertEquals("value", CookieReader.findCookie(request, "wanted").map(Cookie::getValue).orElseThrow());
    }

    @Test
    void returnsTheFirstCookieWhenTheNameOccursTwice() {

        var request = requestWith(new Cookie("wanted", "first"), new Cookie("wanted", "second"));

        assertEquals("first", CookieReader.findCookie(request, "wanted").map(Cookie::getValue).orElseThrow());
    }

    @Test
    void returnsEmptyWhenOnlyOtherCookiesArePresent() {

        var request = requestWith(new Cookie("other", "x"));

        assertEquals(Optional.empty(), CookieReader.findCookie(request, "wanted"));
    }

    @Test
    void returnsEmptyWhenTheRequestHasNoCookies() {

        assertTrue(CookieReader.findCookie(new MockHttpServletRequest(), "wanted").isEmpty());
    }

    @Test
    void matchesTheNameCaseSensitively() {

        var request = requestWith(new Cookie("Wanted", "value"));

        assertTrue(CookieReader.findCookie(request, "wanted").isEmpty());
    }

    private MockHttpServletRequest requestWith(Cookie... cookies) {
        var request = new MockHttpServletRequest();
        request.setCookies(cookies);
        return request;
    }
}

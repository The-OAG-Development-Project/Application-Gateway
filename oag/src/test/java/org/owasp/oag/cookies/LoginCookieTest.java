package org.owasp.oag.cookies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.owasp.oag.session.UserModel;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginCookieTest {

    private static final int EXPIRY = 10_000;

    private static Clock clockAt(long epochSeconds) {
        return Clock.fixed(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private static LoginCookie cookie(UserModel user) {
        var cookie = new LoginCookie(EXPIRY, "github", user, "session-1");
        cookie.setCsrfToken("csrf-1");
        return cookie;
    }

    @Test
    void toSessionCopiesAllValues() {
        var user = new UserModel("alice");

        var session = cookie(user).toSession(clockAt(EXPIRY - 300)).orElseThrow();

        assertEquals(EXPIRY, session.getSessionExpSeconds());
        assertEquals(300, session.getRemainingTimeSeconds());
        assertEquals("github", session.getProvider());
        assertSame(user, session.getUserModel());
        assertEquals("session-1", session.getId());
        assertEquals("csrf-1", session.getCsrfToken());
    }

    @Test
    void expiredCookieGivesNoSession() {
        assertTrue(cookie(new UserModel("alice")).toSession(clockAt(EXPIRY + 1)).isEmpty());
    }

    @Test
    void jsonFormContainsOnlyCookieValues() {
        var json = new ObjectMapper().valueToTree(cookie(new UserModel("alice")));
        var names = new HashSet<String>();
        json.fieldNames().forEachRemaining(names::add);

        assertEquals(Set.of("sessionExpSeconds", "providerKey", "userModel", "csrfToken", "id"), names);
    }
}

package ch.gianlucafrei.nellygateway.utils;

import org.junit.jupiter.api.Test;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CookieUtilsTest {

    @Test
    void getCookieAsHeaderTest() {

        //Arrange
        Cookie cookie = new Cookie("key", "value");
        cookie.setSecure(false);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(333);
        cookie.setPath("/foo");

        Cookie cookie2 = new Cookie("key", "value");
        cookie2.setSecure(true);

        // Act
        String result1 = CookieUtils.getCookieAsHeader(cookie, "strict");
        String result2 = CookieUtils.getCookieAsHeader(cookie2, "");

        // Assert
        assertEquals("key=value; Max-Age=333; HttpOnly; SameSite=strict; Path=/foo", result1);
        assertEquals("key=value; Secure", result2);
    }
}
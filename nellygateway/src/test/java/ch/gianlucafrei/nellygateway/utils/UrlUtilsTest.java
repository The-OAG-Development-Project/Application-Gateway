package ch.gianlucafrei.nellygateway.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlUtilsTest {

    @Test
    public void testIsValidReturnUrl() {

        // WhitelistedUrl
        assertTrue(UrlUtils.isValidReturnUrl("https://user:pass@test.com/abc", new String[]{"abc.ch", "test.com"}));


        // RelativeUrl
        assertTrue(UrlUtils.isValidReturnUrl("/abc?id=45", new String[]{"abc.ch", "test.com"}));

    }

    @Test
    public void testIsInvalidReturnUrl() {

        // Unknown host
        assertFalse(UrlUtils.isValidReturnUrl("https://unknown.com/realtiveUrl", new String[]{"abc.ch", "test.com"}));

        // Invalid url
        assertFalse(UrlUtils.isValidReturnUrl("https://////notvalidUrl/", new String[]{"abc.ch", "test.com"}));

        // No schema
        assertFalse(UrlUtils.isValidReturnUrl("test.com/realtiveUrl", new String[]{"abc.ch", "test.com"}));

        // not https
        assertFalse(UrlUtils.isValidReturnUrl("http://test.com/realtiveUrl", new String[]{"abc.ch", "test.com"}));
    }
}
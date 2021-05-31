package org.owasp.oag.utils;

import org.junit.jupiter.api.Test;
import org.owasp.oag.integration.mockserver.OpenRedirectsTest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlUtilsTest {

    @Test
    public void testIsValidReturnUrl() {

        // WhitelistedUrl
        assertTrue(UrlUtils.isValidReturnUrl("https://user:pass@test.com/abc", new String[]{"abc.ch", "test.com"}));

        // Whitelisted host but different port
        assertTrue(UrlUtils.isValidReturnUrl("https://user:pass@test.com:8080/abc", new String[]{"abc.ch", "test.com"}));
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

        // RelativeUrl
        assertFalse(UrlUtils.isValidReturnUrl("/abc?id=45", new String[]{"abc.ch", "test.com"}));

        // Different Port
        assertFalse(UrlUtils.isValidReturnUrl("https://user:test.com@unknown.com:7777/abc", new String[]{"abc.ch", "test.com"}));
    }

    @Test
    void isValidReturnUrl() throws Exception {

        var testCases = OpenRedirectsTest.loadOpenRedirectTestCases();
        var failedCases = new ArrayList<String>();

        for (var testCase : testCases) {

            var uriString = URLDecoder.decode(testCase, StandardCharsets.UTF_8);

            if (UrlUtils.isValidReturnUrl(uriString, new String[]{"www.whitelisteddomain.tld"}))
                failedCases.add(testCase);
        }

        // This is only used for debugging
        for (var testCase : failedCases) {

            var uriString = URLDecoder.decode(testCase, StandardCharsets.UTF_8);
            UrlUtils.isValidReturnUrl(uriString, new String[]{"www.whitelisteddomain.tld"});
        }

        assertTrue(failedCases.isEmpty(), "Some openRedirects were not rejected: " + failedCases);
    }
}
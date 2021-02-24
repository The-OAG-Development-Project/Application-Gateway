package org.owasp.oag.gateway;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ProxyPathMatcherTest {

    @Test
    public void testRouteMatcher() {

        // Arrange
        ProxyPathMatcher matcher = new ProxyPathMatcher();

        // Act
        assertTrue(matcher.matchesPath("/", "/**"));
        assertTrue(matcher.matchesPath("/foo", "/**"));
        assertTrue(matcher.matchesPath("/foo/bar", "/**"));

        assertTrue(matcher.matchesPath("/foo", "/foo"));
        assertFalse(matcher.matchesPath("/bar", "/foo"));
        assertFalse(matcher.matchesPath("/foo/bar", "/foo"));

        assertTrue(matcher.matchesPath("/foo/bar", "/foo/*"));
        assertFalse(matcher.matchesPath("/foo/bar/abc", "/foo/*"));


        assertTrue(matcher.matchesPath("/foo", "/foo/**"));
        assertTrue(matcher.matchesPath("/foo/bar", "/foo/**"));
        assertTrue(matcher.matchesPath("/foo/bar/", "/foo/**"));
        assertTrue(matcher.matchesPath("/foo/bar/abc", "/foo/**"));
        assertFalse(matcher.matchesPath("/bar", "/foo/**"));
    }

    @Test
    public void testOrderPattersBySpecificity() {

        // Arrange
        ProxyPathMatcher matcher = new ProxyPathMatcher();

        var orderedPatterns = Lists.list("/foo/bar/abc", "/foo", "/foo/bar/*", "/*", "/foo/bar/**", "/foo/**", "/**");
        var patterns = new ArrayList<>(orderedPatterns);
        Collections.shuffle(patterns);

        patterns.sort(matcher.getPatternComparator());

        assertEquals(orderedPatterns, patterns);
    }
}
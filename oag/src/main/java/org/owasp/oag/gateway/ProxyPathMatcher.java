package org.owasp.oag.gateway;

import org.springframework.util.AntPathMatcher;

import java.util.Comparator;

/**
 * Utility class for matching request paths against configuration patterns
 * Uses Spring's AntPathMatcher for pattern matching
 */
public class ProxyPathMatcher {

    private final AntPathMatcher antPathMatcher;

    /**
     * Constructs a ProxyPathMatcher with a case-sensitive AntPathMatcher
     */
    public ProxyPathMatcher() {
        antPathMatcher = new AntPathMatcher();
        antPathMatcher.setCaseSensitive(true);
    }

    /**
     * Checks if a request path matches a configuration pattern
     *
     * @param requestPath The actual request path to check
     * @param configPattern The pattern from configuration to match against
     * @return true if the path matches the pattern, false otherwise
     */
    public boolean matchesPath(String requestPath, String configPattern) {
        boolean isMatch = antPathMatcher.match(configPattern, requestPath);
        return isMatch;
    }

    /**
     * Gets a comparator for sorting patterns by specificity
     *
     * @return A comparator that sorts patterns from most specific to least specific
     */
    public Comparator<String> getPatternComparator() {
        return antPathMatcher.getPatternComparator(null);
    }
}

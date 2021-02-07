package ch.gianlucafrei.nellygateway.gateway;

import org.springframework.util.AntPathMatcher;

import java.util.Comparator;

public class ProxyPathMatcher {

    private final AntPathMatcher antPathMatcher;

    public ProxyPathMatcher() {
        antPathMatcher = new AntPathMatcher();
        antPathMatcher.setCaseSensitive(true);
    }

    public boolean matchesPath(String requestPath, String configPattern) {

        boolean isMatch = antPathMatcher.match(configPattern, requestPath);
        return isMatch;
    }

    public Comparator<String> getPatternComparator() {

        return antPathMatcher.getPatternComparator(null);
    }
}

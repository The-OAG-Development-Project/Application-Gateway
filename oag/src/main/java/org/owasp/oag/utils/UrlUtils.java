package org.owasp.oag.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class UrlUtils {

    public static boolean isValidReturnUrl(String returnUrl, String[] allowedHosts) {

        // Check if relative url
        if (isRelativeUrl(returnUrl)) {
            // Relative url are very hard to parse correctly. i.e. "/\t/example.com"
            return false;
        }

        // Absolute urls
        try {

            URL url = new URL(returnUrl);
            String host = url.getHost();

            // Check if host is in allowed hosts
            boolean isWhitelisted = Arrays.stream(allowedHosts).anyMatch(u -> u != null && u.equals(host));
            if (!isWhitelisted)
                return false;

            // Check if protocol is https
            return "https".equals(url.getProtocol());

        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static boolean isRelativeUrl(String url) {

        if (url == null)
            throw new IllegalArgumentException("url is null");

        return url.startsWith("/");
    }

    public static String getPathOfUrl(String url) {

        var uri = URI.create(url);
        var path = uri.getRawPath();
        return path.endsWith("/") ? path : path + "/";
    }
}

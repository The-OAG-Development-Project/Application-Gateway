package org.owasp.oag.utils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class UrlUtils {

    /**
     * Checks if a url is safe to redirect the user.
     * Relative url -> false
     * Otherwise true if the host is within the allowed hosts array
     * @param returnUrl The url to test (client input)
     * @param allowedHosts Allowed redirect hosts
     * @return True if the returnUrl is safe to redirect the client to
     */
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

    /**
     * Check if a given url string is a relative url
     * @param url The url to check
     * @return True is the url starts with /
     */
    public static boolean isRelativeUrl(String url) {

        if (url == null)
            throw new IllegalArgumentException("url is null");

        return url.startsWith("/");
    }

    /**
     * Returns the path part of a given url string as string
     * @param url Url string to get the path from
     * @return path part of the url. '/' is appended if not present
     */
    public static String getPathOfUrl(String url) {

        var uri = URI.create(url);
        var path = uri.getRawPath();
        return path.endsWith("/") ? path : path + "/";
    }

    /**
     * Replaces the port of a given url and returns a new url object
     * @param original Original url object
     * @param newPort New port
     * @return New url object with updated port
     */
    public static URL replacePort(URL original, int newPort){

        try {
            return new URL(original.getProtocol(), original.getHost(), newPort, original.getFile());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid url", e);
        }
    }

    /**
     * Repleaces the port in a given url string
     * @param original Original url string
     * @param newPort New port
     * @return New url string with updated port
     * @throws MalformedURLException original url string is not a valid url
     */
    public static String replacePortOfUrlString(String original, int newPort) throws MalformedURLException {

        var originalUrl = new URL(original);
        return replacePort(originalUrl, newPort).toString();

    }
}

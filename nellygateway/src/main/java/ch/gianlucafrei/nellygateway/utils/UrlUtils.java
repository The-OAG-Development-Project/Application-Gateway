package ch.gianlucafrei.nellygateway.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class UrlUtils {

    public static boolean isValidReturnUrl(String returnUrl, String[] allowedHosts) {

        // Check if relative url
        if (isRelativeUrl(returnUrl)) {
            // Check if we can compose a full url
            try {
                URL testUrl = new URL(new URL("https:www.testurl.com"), returnUrl);
                return true;
            } catch (MalformedURLException e) {
                return false;
            }

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
            // but we make a exception for localhost urls
            if ("localhost".equals(host))
                return true;
            else
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
}

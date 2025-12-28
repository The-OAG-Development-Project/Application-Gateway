package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.utils.UrlUtils;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a gateway route configuration.
 * A gateway route defines how incoming requests are mapped to backend services,
 * including path matching, target URL, security profile, and path rewriting rules.
 */
public class GatewayRoute implements ErrorValidation {

    private String path;
    private String url;
    private String type;
    private boolean allowAnonymous;
    private PathRewriteConfig rewrite = PathRewriteConfig.defaultConfig();

    /**
     * Default constructor for deserialization.
     */
    public GatewayRoute() {
    }

    /**
     * Creates a fully configured gateway route.
     *
     * @param path The path pattern to match for incoming requests
     * @param url The target URL to route matched requests to
     * @param type The security profile type to apply to this route
     * @param allowAnonymous Whether anonymous (unauthenticated) access is allowed
     * @param rewrite The path rewrite configuration for URL transformation
     */
    public GatewayRoute(String path, String url, String type, boolean allowAnonymous, PathRewriteConfig rewrite) {
        this.path = path;
        this.url = url;
        this.type = type;
        this.allowAnonymous = allowAnonymous;

        if(rewrite != null)
            this.rewrite = rewrite;
    }

    /**
     * Gets the path pattern for matching incoming requests.
     *
     * @return The path pattern string
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path pattern for matching incoming requests.
     *
     * @param path The path pattern string
     */
    private void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the target URL to route matched requests to.
     *
     * @return The target URL string
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the target URL to route matched requests to.
     *
     * @param url The target URL string
     */
    private void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the security profile type to apply to this route.
     *
     * @return The security profile type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the security profile type to apply to this route.
     *
     * @param type The security profile type
     */
    private void setType(String type) {
        this.type = type;
    }

    /**
     * Checks if anonymous (unauthenticated) access is allowed for this route.
     *
     * @return true if anonymous access is allowed, false otherwise
     */
    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    /**
     * Sets whether anonymous (unauthenticated) access is allowed for this route.
     *
     * @param allowAnonymous true to allow anonymous access, false otherwise
     */
    private void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    /**
     * Gets the path rewrite configuration for URL transformation.
     *
     * @return The path rewrite configuration
     */
    public PathRewriteConfig getRewrite() {
        return rewrite;
    }

    /**
     * Sets the path rewrite configuration for URL transformation.
     *
     * @param rewrite The path rewrite configuration
     */
    private void setRewrite(PathRewriteConfig rewrite) {
        this.rewrite = rewrite;
    }

    /**
     * Validates the gateway route configuration and returns any errors found.
     *
     * @param context The application context
     * @return A list of validation error messages, empty if no errors are found
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {
        var errors = new ArrayList<String>();

        if (path == null)
            errors.add("path not defined");

        if (url == null)
            errors.add("url not defined");

        if (type == null)
            errors.add("type not defined");

        if (rewrite == null)
            errors.add("path rewrite config not defined");

        // Dont continue with validation if fields are missing
        if (!errors.isEmpty())
            return errors;

        errors.addAll(rewrite.getErrors(context));

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            errors.add("invalid url");
        }

        return errors;
    }

    /**
     * Returns the path of the route without trailing wildcards.
     * This removes '*' or '**' pattern matching suffixes from the path.
     *
     * @return The base path without wildcards
     */
    public String getPathBase() {

        int wildcardStringLength = 0;

        if (path.endsWith("*"))
            wildcardStringLength = 1;

        if (path.endsWith("**"))
            wildcardStringLength = 2;

        return path.substring(0, path.length() - wildcardStringLength);
    }

    /**
     * Returns the path portion of the target URL.
     * This extracts just the path component from the complete URL.
     *
     * @return The path portion of the target URL
     */
    public String getUrlPath() {

        return UrlUtils.getPathOfUrl(url);
    }
}

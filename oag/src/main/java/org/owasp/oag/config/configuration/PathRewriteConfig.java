package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for path rewriting in gateway routes.
 * This class defines how incoming request paths are transformed when forwarded to backend services,
 * using regex patterns and replacements.
 */
public class PathRewriteConfig implements ErrorValidation {

    private String regex;
    private String replacement;

    /**
     * Creates a default path rewrite configuration.
     * The default configuration preserves path segments after the base path.
     *
     * @return A default path rewrite configuration
     */
    public static PathRewriteConfig defaultConfig() {

        var config = new PathRewriteConfig();

        config.setRegex("<route-path-base>(?<segment>.*)");
        config.setReplacement("<route-uri-path>${segment}");

        return config;
    }

    /**
     * Builds a concrete PathRewriteConfig instance for a specific route.
     * This resolves placeholders in the regex and replacement patterns using the route's properties.
     *
     * @param route The route to build the path rewrite configuration for
     * @return A resolved path rewrite configuration
     */
    public PathRewriteConfig build(GatewayRoute route) {

        var config = new PathRewriteConfig();

        config.setRegex(substitutePlaceholder(this.getRegex(), route));
        config.setReplacement(substitutePlaceholder(this.getReplacement(), route));

        return config;
    }

    /**
     * Substitutes placeholders in a string with values from the route.
     * Replaces &lt;route-path-base&gt; with the route's base path and
     * &lt;route-uri-path&gt; with the route's URL path.
     *
     * @param str The string containing placeholders
     * @param route The route providing values for placeholders
     * @return The string with placeholders replaced
     */
    private static String substitutePlaceholder(String str, GatewayRoute route) {

        str = str.replaceAll("<route-path-base>", route.getPathBase());
        str = str.replaceAll("<route-uri-path>", route.getUrlPath());
        return str;
    }

    /**
     * Gets the regex pattern for path matching.
     *
     * @return The regex pattern string
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Sets the regex pattern for path matching.
     *
     * @param regex The regex pattern string
     */
    private void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * Gets the replacement pattern for path transformation.
     *
     * @return The replacement pattern string
     */
    public String getReplacement() {
        return replacement;
    }

    /**
     * Sets the replacement pattern for path transformation.
     *
     * @param replacement The replacement pattern string
     */
    private void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * Validates the path rewrite configuration and returns any errors found.
     *
     * @param context The application context
     * @return A list of validation error messages, empty if no errors are found
     */
    @Override
    public List<String> getErrors(ApplicationContext context) {

        var errors = new LinkedList<String>();

        if (regex == null || regex.isEmpty())
            errors.add("Rewrite regex must not be empty");

        if (replacement == null || replacement.isEmpty())
            errors.add("Rewrite replacement must not be empty");

        return errors;
    }
}

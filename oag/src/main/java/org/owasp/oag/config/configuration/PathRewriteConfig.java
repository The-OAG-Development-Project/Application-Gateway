package org.owasp.oag.config.configuration;

import org.owasp.oag.config.Subconfig;
import org.springframework.context.ApplicationContext;

import java.util.LinkedList;
import java.util.List;

public class PathRewriteConfig implements Subconfig {

    private String regex;
    private String replacement;

    public static PathRewriteConfig defaultConfig() {

        var config = new PathRewriteConfig();

        config.setRegex("<route-path-base>(?<segment>.*)");
        config.setReplacement("<route-uri-path>${segment}");

        return config;
    }

    public PathRewriteConfig build(GatewayRoute route) {

        var config = new PathRewriteConfig();

        config.setRegex(substitutePlaceholder(this.getRegex(), route));
        config.setReplacement(substitutePlaceholder(this.getReplacement(), route));

        return config;
    }

    private static String substitutePlaceholder(String str, GatewayRoute route) {

        str = str.replaceAll("<route-path-base>", route.getPathBase());
        str = str.replaceAll("<route-uri-path>", route.getUrlPath());
        return str;
    }

    public String getRegex() {
        return regex;
    }

    private void setRegex(String regex) {
        this.regex = regex;
    }

    public String getReplacement() {
        return replacement;
    }

    private void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    @Override
    public List<String> getErrors(ApplicationContext context, MainConfig rootConfig) {

        var errors = new LinkedList<String>();

        if (regex == null || regex.isEmpty())
            errors.add("Rewrite regex must not be empty");

        if (replacement == null || replacement.isEmpty())
            errors.add("Rewrite replacement must not be empty");

        return errors;
    }
}

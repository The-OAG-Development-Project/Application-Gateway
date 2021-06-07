package org.owasp.oag.config.configuration;

import org.owasp.oag.config.ErrorValidation;
import org.owasp.oag.utils.UrlUtils;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GatewayRoute implements ErrorValidation {

    private String path;
    private String url;
    private String type;
    private boolean allowAnonymous;
    private PathRewriteConfig rewrite = PathRewriteConfig.defaultConfig();

    public GatewayRoute() {
    }

    public GatewayRoute(String path, String url, String type, boolean allowAnonymous, PathRewriteConfig rewrite) {
        this.path = path;
        this.url = url;
        this.type = type;
        this.allowAnonymous = allowAnonymous;

        if(rewrite != null)
            this.rewrite = rewrite;
    }

    public String getPath() {
        return path;
    }

    private void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    private void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    private void setAllowAnonymous(boolean allowAnonymous) {
        this.allowAnonymous = allowAnonymous;
    }

    public PathRewriteConfig getRewrite() {
        return rewrite;
    }

    private void setRewrite(PathRewriteConfig rewrite) {
        this.rewrite = rewrite;
    }

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
     * Returns the path of the route without trailing wildcards
     *
     * @return
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
     * Returns the path of the url of the route
     *
     * @return
     */
    public String getUrlPath() {

        return UrlUtils.getPathOfUrl(url);
    }
}

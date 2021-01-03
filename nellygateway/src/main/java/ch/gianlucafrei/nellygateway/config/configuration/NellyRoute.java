package ch.gianlucafrei.nellygateway.config.configuration;

import ch.gianlucafrei.nellygateway.config.ErrorValidation;
import org.springframework.context.ApplicationContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NellyRoute implements ErrorValidation {

    private String path;
    private String url;
    private String type;
    private boolean allowAnonymous;

    public NellyRoute() {
    }

    public NellyRoute(String path, String url, String type, boolean allowAnonymous) {
        this.path = path;
        this.url = url;
        this.type = type;
        this.allowAnonymous = allowAnonymous;
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

    @Override
    public List<String> getErrors(ApplicationContext context) {
        var errors = new ArrayList<String>();

        if (path == null)
            errors.add("path not defined");

        if (url == null)
            errors.add("url not defined");

        if (type == null)
            errors.add("type not defined");

        // Dont continue with validation if fields are missing
        if (!errors.isEmpty())
            return errors;

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            errors.add("invalid url");
        }

        return errors;
    }
}

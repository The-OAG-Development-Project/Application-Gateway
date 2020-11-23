package ch.gianlucafrei.nellygateway.config.configuration;

public class NellyRoute {

    private String path;
    private String url;
    private String type;
    private boolean allowAnonymous;

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
}

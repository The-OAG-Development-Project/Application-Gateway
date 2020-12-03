package ch.gianlucafrei.nellygateway.filters.session;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface NellySessionFilter {

    int order();

    void createSession(Map<String, Object> filterContext, HttpServletResponse response);

    void destroySession(Map<String, Object> filterContext, HttpServletResponse response);
}

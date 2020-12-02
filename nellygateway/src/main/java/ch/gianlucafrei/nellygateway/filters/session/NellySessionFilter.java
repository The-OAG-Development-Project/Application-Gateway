package ch.gianlucafrei.nellygateway.filters.session;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface NellySessionFilter {

    int order();

    void doFilter(Map<String, Object> filterContext, HttpServletResponse response);
}

package ch.gianlucafrei.nellygateway.filters.session;

import ch.gianlucafrei.nellygateway.session.Session;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface NellySessionFilter {

    int order();

    void createSession(Map<String, Object> filterContext, HttpServletResponse response);

    void destroySession(Map<String, Object> filterContext, HttpServletResponse response);

    void renewSession(Map<String, Object> filterContext, HttpServletResponse response);

    static List<NellySessionFilter> getNellySessionFilters(ApplicationContext context) {

        var filters = context.getBeansOfType(NellySessionFilter.class);

        List<NellySessionFilter> sessionFilters = filters.values().stream()
                .sorted(Comparator.comparingInt(NellySessionFilter::order))
                .collect(Collectors.toList());

        return sessionFilters;
    }

    static void runRenewSessionFilterChain(ApplicationContext context, Map<String, Object> filterContext, HttpServletResponse response){

        var filters = getNellySessionFilters(context);
        filters.forEach(f -> f.renewSession(filterContext, response));
    }
}

package ch.gianlucafrei.nellygateway.filters.zuul.post;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.NellyRoute;
import ch.gianlucafrei.nellygateway.config.configuration.SecurityProfile;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ResponseHeaderFilter extends ZuulFilter {

    @Autowired
    private NellyConfig config;

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();

        // Load security profile
        String routeName = (String) ctx.get("proxy");
        NellyRoute nellyRoute = config.getRoutes().get(routeName);
        SecurityProfile securityProfile = config.getSecurityProfiles().get(nellyRoute.getType());

        // Load headers
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();

        // Change headers according to security policy
        for (Map.Entry<String, String> entry : securityProfile.getResponseHeaders().entrySet()) {

            String name = entry.getKey();
            String value = entry.getValue();

            if ("<<remove>>".equals(value)) {
                zuulResponseHeaders = removeHeader(zuulResponseHeaders, name);
            } else {
                zuulResponseHeaders.add(new Pair<>(name, value));
            }
        }

        ctx.put("zuulResponseHeaders", zuulResponseHeaders);
        return null;
    }

    private List<Pair<String, String>> removeHeader(List<Pair<String, String>> zuulResponseHeaders, String name) {

        return zuulResponseHeaders.stream()
                .filter(pair -> !pair.first().equals(name))
                .collect(Collectors.toList());
    }
}

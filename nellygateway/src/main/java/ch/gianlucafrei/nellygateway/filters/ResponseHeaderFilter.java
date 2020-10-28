package ch.gianlucafrei.nellygateway.filters;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyRoute;
import ch.gianlucafrei.nellygateway.config.SecurityProfile;
import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.utils.JWEGenerator;
import com.netflix.util.Pair;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseHeaderFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);
    JWEGenerator jweGenerator = new JWEGenerator();

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
        NellyRoute nellyRoute = NellygatewayApplication.config.routes.get(routeName);
        SecurityProfile securityProfile = NellygatewayApplication.config.securityProfiles.get(nellyRoute.type);

        // Load headers
        List<Pair<String, String>> zuulResponseHeaders = ctx.getZuulResponseHeaders();

        // Change headers according to security policy
        for (Map.Entry<String, String> entry : securityProfile.headers.entrySet()) {

            String name = entry.getKey();
            String value = entry.getValue();

            if("<<remove>>".equals(value)){
                zuulResponseHeaders = removeHeader(zuulResponseHeaders, name);
            }
            else {
                zuulResponseHeaders.add(new Pair<>(name, value));
            }
        }

        ctx.put("zuulResponseHeaders", zuulResponseHeaders);
        return null;
    }

    private List<Pair<String, String>> removeHeader(List<Pair<String, String>> zuulResponseHeaders, String name){

        return zuulResponseHeaders.stream()
                .filter(pair -> !pair.first().equals(name))
                .collect(Collectors.toList());
    }
}

package ch.gianlucafrei.nellygateway.filters.zuul.route;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.NellyRoute;
import ch.gianlucafrei.nellygateway.config.configuration.SecurityProfile;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Component
public class MethodWhitelistFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(MethodWhitelistFilter.class);

    @Autowired
    private NellyConfig config;

    @Override
    public String filterType() {
        return "route";
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
        HttpServletRequest request = ctx.getRequest();

        // Load security profile
        String routeName = (String) ctx.get("proxy");
        NellyRoute nellyRoute = config.getRoutes().get(routeName);
        String type = nellyRoute.getType();
        SecurityProfile securityProfile = config.getSecurityProfiles().get(type);
        List<String> allowedMethods = securityProfile.getAllowedMethods();

        String reqMethod = request.getMethod();
        // Check if method is allowed
        boolean isAllowed = allowedMethods.stream().anyMatch(m -> m.equals(reqMethod));

        if (!isAllowed) {

            ctx.setSendZuulResponse(false);
            HttpServletResponse response = ctx.getResponse();
            response.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);

            log.info("Blocked request because method was not allowed, route={}, reqMethod={}, allowedMethods={}",
                    routeName,
                    reqMethod,
                    Arrays.toString(allowedMethods.toArray()));
        }

        return null;
    }
}

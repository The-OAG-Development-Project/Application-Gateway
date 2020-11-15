package ch.gianlucafrei.nellygateway.filters.zuul;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyRoute;
import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class AccessControlFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(AccessControlFilter.class);

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

        // Load nelly route
        String routeName = (String) ctx.get("proxy");
        NellyRoute nellyRoute = NellygatewayApplication.config.routes.get(routeName);

        // Load session
        Optional<Session> sessionOptional = (Optional<Session>) request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if(nellyRoute.allowAnonymous)
            return null;

        if(! sessionOptional.isPresent())
        {
            ctx.setSendZuulResponse(false);
            ctx.setResponseBody("not authorized");
            ctx.getResponse().setHeader("Content-Type", "text/plain;charset=UTF-8");
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            log.info("Blocked unauthenticated request {} {}", request.getMethod(), request.getRequestURI());
        }

        return null;
    }
}

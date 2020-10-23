package ch.gianlucafrei.nellygateway.filters;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.utils.JWEGenerator;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class AuthenticationFilter extends ZuulFilter {

    JWEGenerator jweGenerator = new JWEGenerator();

    private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        ctx.addZuulRequestHeader("X-PROXY", "Nellygateway");
        ctx.addZuulRequestHeader("X-NELLY-ApiKey", NellygatewayApplication.config.nellyApiKey);

        SessionCookie sessionCookie = SessionCookie.loadFromRequest(request, jweGenerator);

        if(sessionCookie == null)
        {
            ctx.addZuulRequestHeader("X-NELLY-Status", "Anonymous");
        }
        else
        {
            ctx.addZuulRequestHeader("X-NELLY-Status", "Authenticated");
            ctx.addZuulRequestHeader("X-NELLY-User", sessionCookie.getSubject());
            ctx.addZuulRequestHeader("X-NELLY-Provider", sessionCookie.getProvider());
            ctx.addZuulRequestHeader("X-NELLY-OriginalToken", sessionCookie.getOrginalToken());
        }

        return null;
    }

}
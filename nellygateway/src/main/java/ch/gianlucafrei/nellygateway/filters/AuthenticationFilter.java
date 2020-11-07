package ch.gianlucafrei.nellygateway.filters;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SimpleLogFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

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

        SessionCookie sessionCookie = SessionCookie.loadFromRequest(request, cookieEncryptor);

        if (sessionCookie == null) {
            ctx.addZuulRequestHeader("X-NELLY-Status", "Anonymous");
        } else {
            ctx.addZuulRequestHeader("X-NELLY-Status", "Authenticated");
            ctx.addZuulRequestHeader("X-NELLY-User", sessionCookie.getSubject());
            ctx.addZuulRequestHeader("X-NELLY-Provider", sessionCookie.getProvider());
            ctx.addZuulRequestHeader("X-NELLY-OriginalToken", sessionCookie.getOrginalToken());
        }

        return null;
    }

}
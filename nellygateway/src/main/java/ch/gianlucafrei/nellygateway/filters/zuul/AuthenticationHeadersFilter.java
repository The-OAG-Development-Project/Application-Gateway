package ch.gianlucafrei.nellygateway.filters.zuul;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.filters.spring.ExtractAuthenticationFilter;
import ch.gianlucafrei.nellygateway.session.Session;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;

@Component
public class AuthenticationHeadersFilter extends ZuulFilter {

    @Autowired
    private NellyConfig nellyConfig;

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
        ctx.addZuulRequestHeader("X-NELLY-ApiKey", nellyConfig.getNellyApiKey());

        Optional<Session> sessionOptional = (Optional<Session>)request.getAttribute(ExtractAuthenticationFilter.NELLY_SESSION);

        if(sessionOptional.isPresent())
        {
            Session session = sessionOptional.get();
            ctx.addZuulRequestHeader("X-NELLY-Status", "Authenticated");
            ctx.addZuulRequestHeader("X-NELLY-Provider", session.getProvider());

            HashMap<String, String> userMappings = session.getUserModel().getMappings();

            userMappings.entrySet()
                    .forEach(mapping -> ctx.addZuulRequestHeader(
                            "X-NELLY-USER-" + mapping.getKey(),
                            mapping.getValue()));
        }
        else {
            ctx.addZuulRequestHeader("X-NELLY-Status", "Anonymous");
        }

        return null;
    }
}
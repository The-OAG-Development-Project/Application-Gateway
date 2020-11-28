package ch.gianlucafrei.nellygateway.filters.zuul.post;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ZuulDebugResponseFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(ZuulDebugResponseFilter.class);

    @Autowired
    private NellyConfig config;

    @Override
    public String filterType() {
        return "post";
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
        HttpServletResponse response = ctx.getResponse();

        return null;
    }
}

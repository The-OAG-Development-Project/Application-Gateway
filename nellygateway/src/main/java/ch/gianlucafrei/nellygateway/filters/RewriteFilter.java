package ch.gianlucafrei.nellygateway.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class RewriteFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(RewriteFilter.class);

    @Override
    public String filterType() {
        return "route";
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

        RequestContext context = RequestContext.getCurrentContext();

        try {
            context.setRouteHost(new URL("http://localhost:9000/sampleApp/"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return null;
    }
}

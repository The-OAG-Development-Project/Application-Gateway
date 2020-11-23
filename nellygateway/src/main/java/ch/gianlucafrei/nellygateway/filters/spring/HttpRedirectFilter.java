package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// TODO add flag to ignore this filter
@Order(2)
@Component
public class HttpRedirectFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(HttpRedirectFilter.class);

    @Autowired
    private NellyConfig config;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        String hostUri = config.hostUri;
        if(hostUri.startsWith("https://"))
        {
            // We do the request only if we are on https
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;

            if(isInsecureRequest(req))
            {
                sendHttpsRedirectResponse(req, res);
                log.debug("Redirected insecure request to {}", req.getPathInfo());
                return;
            }
        }

        chain.doFilter(request, response);
    }

    public void sendHttpsRedirectResponse(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String path = config.hostUri + req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
        res.sendRedirect(path);
    }

    public boolean isInsecureRequest(HttpServletRequest request) {

        // Check if request was forwarded
        if(request.getHeader("X-Forwarded-For") != null){

            // if X-Forwarded-Proto: https we threat the request as secure
            return ! "https".equals(request.getHeader("X-Forwarded-Proto"));
        }

        // Check if localhost
        if("localhost".equals(request.getServerName()))
            return false;

        // Check protocol
        if("http".equals(request.getScheme()))
            return true;

        if("https".equals(request.getScheme()))
            return false;

        // Fallback if everything else fails (don't redirect)
        log.debug("Unsecure if request if https");
        return false;
    }
}
package ch.gianlucafrei.nellygateway.filters.spring;

import ch.gianlucafrei.nellygateway.cookies.SessionCookie;
import ch.gianlucafrei.nellygateway.services.crypto.CookieEncryptor;
import ch.gianlucafrei.nellygateway.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@Order(3)
@Component
public class ExtractAuthenticationFilter implements Filter {

    public final static String NELLY_SESSION = "nelly-session"; // Key for request context

    private static Logger log = LoggerFactory.getLogger(ExtractAuthenticationFilter.class);

    @Autowired
    CookieEncryptor cookieEncryptor;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // Extract session from cookie
        SessionCookie sessionCookie = SessionCookie.loadFromRequest(req, cookieEncryptor);
        Optional<Session> sessionOptional = Session.fromSessionCookie(sessionCookie);

        // Store session optional in http request object
        req.setAttribute(NELLY_SESSION, sessionOptional);

        // Process other filters
        chain.doFilter(request, response);
    }
}
package ch.gianlucafrei.nellygateway.filters.zuul.route;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.NellyRoute;
import ch.gianlucafrei.nellygateway.config.configuration.SecurityProfile;
import ch.gianlucafrei.nellygateway.services.csrf.CsrfProtectionValidation;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CsrfValidationFilter extends ZuulFilter {

    private static final Logger log = LoggerFactory.getLogger(CsrfValidationFilter.class);

    @Autowired
    private NellyConfig config;

    @Autowired
    private ApplicationContext context;

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
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        // Load security profile
        String routeName = (String) ctx.get("proxy");
        NellyRoute nellyRoute = config.getRoutes().get(routeName);
        String type = nellyRoute.getType();
        SecurityProfile securityProfile = config.getSecurityProfiles().get(type);

        // Find out if we should validate the request

        String reqMethod = request.getMethod();
        boolean isSafeMethod = securityProfile.getCsrfSafeMethods()
                .contains(reqMethod);

        if (!isSafeMethod) {
            String csrfProtectionMethod = securityProfile.getCsrfProtection();
            CsrfProtectionValidation csrfValidation = loadValidationImplementation(csrfProtectionMethod);

            if (csrfValidation.shouldBlockRequest(request)) {

                ctx.setSendZuulResponse(false);
                HttpServletResponse response = ctx.getResponse();
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);

                log.info("Blocked request due to csrf protection, route={}, reqMethod={}, csrfMethod={}",
                        routeName,
                        reqMethod,
                        csrfProtectionMethod);
            }
        }

        return null;
    }

    private CsrfProtectionValidation loadValidationImplementation(String csrfProtectionMethod) {

        String beanname = csrfProtectionMethod + "-validation";
        CsrfProtectionValidation validationImplementation = context.getBean(CsrfProtectionValidation.class, beanname);

        if (validationImplementation == null) {
            throw new RuntimeException("csrf validation implementation not found: " + beanname);
        }

        return validationImplementation;
    }
}

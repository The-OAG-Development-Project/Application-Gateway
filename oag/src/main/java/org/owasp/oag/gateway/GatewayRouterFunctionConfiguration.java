package org.owasp.oag.gateway;

import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.filters.proxy.RouteAwareFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

import java.util.List;

/**
 * Builds the gateway's {@link RouterFunction} from the configured routes.
 * <p>
 * Spring Cloud Gateway does not order routes itself, so the routes are added most-specific-path-first
 * using the {@link ProxyPathMatcher} pattern comparator. OAG's route filters are attached to every route
 * in {@code @Order} sequence.
 */
@Configuration
public class GatewayRouterFunctionConfiguration {

    /**
     * Attribute name under which the name of the matched route is stored in the request attributes.
     */
    public final static String ATTRIBUTE_ROUTE_NAME = "RouteName";

    private static final Logger log = LoggerFactory.getLogger(GatewayRouterFunctionConfiguration.class);

    private final ProxyPathMatcher matcher = new ProxyPathMatcher();
    private final MainConfig config;
    private final List<RouteAwareFilter> routeFilters;

    /**
     * Creates the configuration.
     *
     * @param config       The main configuration holding the routes
     * @param routeFilters All OAG route filters, sorted by their {@code @Order} annotation
     */
    public GatewayRouterFunctionConfiguration(MainConfig config, List<RouteAwareFilter> routeFilters) {

        this.config = config;
        this.routeFilters = routeFilters.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    /**
     * Creates the router function serving all configured gateway routes.
     *
     * @return The combined router function
     */
    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        var patternComparator = matcher.getPatternComparator();

        return config.getRoutes().entrySet().stream()
                .sorted((a, b) -> patternComparator.compare(a.getValue().getPath(), b.getValue().getPath()))
                .map(entry -> routeFor(entry.getKey(), entry.getValue()))
                .reduce(RouterFunction::and)
                .orElseGet(GatewayRouterFunctionConfiguration::noRoutes);
    }

    /**
     * Moves the router function mapping behind the annotated controllers.
     * <p>
     * Spring MVC registers {@code RouterFunctionMapping} at order -1, ahead of the
     * {@code RequestMappingHandlerMapping} that serves OAG's own endpoints. A configured route such as
     * {@code /**} would therefore proxy {@code /auth/...} downstream instead of letting the login
     * controller handle it. Order 1 restores the intended precedence: OAG's own endpoints first, then
     * the configured gateway routes, then static resources.
     *
     * @return The post processor that reorders the mapping
     */
    @Bean
    public static BeanPostProcessor routerFunctionMappingAfterControllers() {

        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RouterFunctionMapping mapping) {
                    mapping.setOrder(1);
                }
                return bean;
            }
        };
    }

    private RouterFunction<ServerResponse> routeFor(String routeName, GatewayRoute route) {

        var rewrite = route.getRewrite().build(route);

        // The registration order is the execution order: routeId -> route-name attribute -> uri ->
        // rewritePath -> the OAG filters in @Order sequence -> the proxy call. rewritePath must run
        // after uri (it records the original request url that RouteAwareFilter reads) and before the
        // OAG filters (which see the rewritten path).
        var builder = GatewayRouterFunctions.route(routeName)
                .route(request -> matches(routeName, route, request), HandlerFunctions.http())
                .before(BeforeFilterFunctions.routeId(routeName))
                .before(request -> {
                    MvcUtils.putAttribute(request, ATTRIBUTE_ROUTE_NAME, routeName);
                    return request;
                })
                .before(BeforeFilterFunctions.uri(route.getUrl()))
                .before(BeforeFilterFunctions.rewritePath(rewrite.getRegex(), rewrite.getReplacement()));

        for (RouteAwareFilter filter : routeFilters) {
            builder = builder.filter(filter);
        }

        log.debug("Initialized gateway route {}", routeName);
        return builder.build();
    }

    private boolean matches(String routeName, GatewayRoute route, ServerRequest request) {

        var requestPath = request.uri().getPath();
        var isMatch = matcher.matchesPath(requestPath, route.getPath());
        log.trace("Evaluate route {} for {} -> {}", routeName, requestPath, isMatch ? "match" : "false");
        return isMatch;
    }

    private static RouterFunction<ServerResponse> noRoutes() {
        return RouterFunctions.route(request -> false, request -> ServerResponse.notFound().build());
    }
}

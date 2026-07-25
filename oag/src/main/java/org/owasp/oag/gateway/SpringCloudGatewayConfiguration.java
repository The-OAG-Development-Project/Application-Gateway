package org.owasp.oag.gateway;

import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.owasp.oag.utils.LoggingUtils.logTrace;

/**
 * This class initializes the Spring Cloud Gateway. It creates the "gatewayRoutes" which contains all the routing information.
 */
@Configuration
public class SpringCloudGatewayConfiguration {

    /**
     * Attribute name for storing the route name in the exchange attributes
     */
    public final static String ATTRIBUTE_ROUTE_NAME = "RouteName";
    private static final Logger log = LoggerFactory.getLogger(SpringCloudGatewayConfiguration.class);
    @Autowired
    private MainConfig config;

    private final ProxyPathMatcher matcher = new ProxyPathMatcher();

    /**
     * Creates the route locator for the gateway
     * Initializes routes from the configuration
     *
     * @param builder The route locator builder
     * @return The configured route locator
     */
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        RouteLocatorBuilder.Builder routes = builder.routes();

        initRoutesFromConfig(routes);

        return routes.build();
    }

    private RouteLocatorBuilder.Builder initRoutesFromConfig(RouteLocatorBuilder.Builder routesBuilder) {

        var routes = routesBuilder;

        // Sort configuration routes by specificity
        // Spring cloud gateway does not care about that
        var patternComparator = matcher.getPatternComparator();
        var configRoutes = config.getRoutes();

        List<Map.Entry<String, GatewayRoute>> sortedConfigRoutes = configRoutes.entrySet()
                .stream().sorted((p1, p2) -> patternComparator.compare(p1.getValue().getPath(), p2.getValue().getPath()))
                .collect(Collectors.toList());

        // Add the routes within their order of specificity
        for (var entry : sortedConfigRoutes) {

            var routeName = entry.getKey();
            var route = entry.getValue();
            var routePath = entry.getValue().getPath();
            var routeUrl = entry.getValue().getUrl();

            routes.route(r -> {

                // Add route predicate that uses the ProxyPathMatcher to find out if a route matches a request
                var path = r.predicate(exchange -> {
                    var requestUrl = exchange.getRequest().getURI().getPath();
                    var isMatch = matcher.matchesPath(requestUrl, routePath);
                    logTrace(log, exchange, "Evaluate route {} for {} -> {}", routeName, requestUrl, isMatch ? "match" : "false");
                    return isMatch;
                });

                var rewriteConfig = route.getRewrite().build(route);
                // Write the route-name attribute in a filter instead of as a side effect of predicate
                // evaluation, because a predicate must stay pure: the functional router restores the
                // attribute map after evaluating a non-matching predicate.
                // HIGHEST_PRECEDENCE is required, not decorative. Every RouteAwareFilter subclass reads
                // this attribute to resolve the route's security profile, and those are GlobalFilters at
                // @Order 10..100. Spring Cloud Gateway merges route filters and global filters into one
                // list and sorts them together, so this must outrank all of them or the wrong security
                // profile gets applied.
                var filters = path.filters(f -> f.filter((exchange, chain) -> {
                            exchange.getAttributes().put(ATTRIBUTE_ROUTE_NAME, routeName);
                            return chain.filter(exchange);
                        }, Ordered.HIGHEST_PRECEDENCE)
                        .rewritePath(rewriteConfig.getRegex(), rewriteConfig.getReplacement()));

                return filters.uri(routeUrl);

            });

            log.debug("Initialized gateway route {}", routeName);
        }

        return routes;
    }
}

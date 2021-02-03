package ch.gianlucafrei.nellygateway.gateway;

import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.NellyRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class initializes the Spring Cloud Gateway. It creates the "gatewayRoutes" which contains all the routing information.
 */
@Configuration
public class SpringCloudGatewayConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SpringCloudGatewayConfiguration.class);

    @Autowired
    NellyConfig config;

    public final static String ATTRIBUTE_ROUTE_NAME = "RouteName";

    private ProxyPathMatcher matcher = new ProxyPathMatcher();

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

        List<Map.Entry<String, NellyRoute>> sortedConfigRoutes = configRoutes.entrySet()
                .stream().sorted((p1, p2) -> patternComparator.compare(p1.getValue().getPath(), p2.getValue().getPath()))
                .collect(Collectors.toList());

        // Add the routes within their order of specificity
        for (var entry : sortedConfigRoutes) {

            var routeName = entry.getKey();
            var routePath = entry.getValue().getPath();
            var routeUrl = entry.getValue().getUrl();
            var routeUrlPath = URI.create(routeUrl).getPath();

            routes.route(r -> {

                // Add route predicate that uses the ProxyPathMatcher to find out if a route matches a request
                var path = r.predicate(exchange -> {
                    exchange.getAttributes().put(ATTRIBUTE_ROUTE_NAME, routeName);
                    var requestUrl = exchange.getRequest().getURI().getPath();
                    log.trace("Evaluate route {} for {}", routeName, requestUrl);
                    return matcher.matchesPath(requestUrl, routePath);
                });

                // If the route contains a wildcard (or two) at the end we rewrite the upstream path using the rewritePat Filter.
                if (routePath.endsWith("*")) {

                    int wildcardStringLength = 1;

                    if (routePath.endsWith("**"))
                        wildcardStringLength = 2;

                    var rewriteRegex = routePath.substring(0, routePath.length() - wildcardStringLength) + "(?<segment>.*)";
                    var rewriteReplacement = "/${segment}";

                    log.info("Route {}: Pattern {} will be replaced with {}", routeName, rewriteRegex, rewriteReplacement);

                    var pathWithFilter = path.filters(rw -> rw.rewritePath(rewriteRegex, rewriteReplacement));

                    return pathWithFilter.uri(routeUrl);
                } else {
                    return path.uri(routeUrl);
                }

            });

            log.info("Initialized gateway route {}", routeName);
        }

        return routes;
    }
}

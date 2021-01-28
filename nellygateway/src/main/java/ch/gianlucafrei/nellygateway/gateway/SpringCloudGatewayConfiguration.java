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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SpringCloudGatewayConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SpringCloudGatewayConfiguration.class);

    @Autowired
    NellyConfig config;

    private ProxyPathMatcher matcher = new ProxyPathMatcher();

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {

        RouteLocatorBuilder.Builder routes = builder.routes();

        initRoutesFromConfig(routes);

        return routes.build();
    }

    private RouteLocatorBuilder.Builder initRoutesFromConfig(RouteLocatorBuilder.Builder routesBuilder) {

        var routes = routesBuilder;

        // Sort configuration routes by specificity
        var patternComparator = matcher.getPatternComparator();
        var configRoutes = config.getRoutes();

        List<Map.Entry<String, NellyRoute>> sortedConfigRoutes = configRoutes.entrySet()
                .stream().sorted((p1, p2) -> patternComparator.compare(p1.getValue().getPath(), p2.getValue().getPath()))
                .collect(Collectors.toList());

        for (var entry : sortedConfigRoutes) {

            var routeName = entry.getKey();
            var routePath = entry.getValue().getPath();
            var routeUrl = entry.getValue().getUrl();

            routes.route(r -> {

                var path = r.predicate(exchange -> {
                    exchange.getAttributes().put("RouteName", routeName);
                    var requestUrl = exchange.getRequest().getURI().getPath();
                    log.trace("Evaluate route {} for {}", routeName, requestUrl);
                    return matcher.matchesPath(requestUrl, routePath);
                });

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

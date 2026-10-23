package org.owasp.oag.gateway;

import org.junit.jupiter.api.Test;
import org.owasp.oag.config.configuration.GatewayRoute;
import org.owasp.oag.config.configuration.MainConfig;
import org.owasp.oag.config.configuration.PathRewriteConfig;
import org.owasp.oag.cookies.CsrfCookie;
import org.owasp.oag.filters.GatewayRouteContext;
import org.owasp.oag.filters.proxy.RouteAwareFilter;
import org.owasp.oag.integration.testInfrastructure.TestFileConfigLoader;
import org.owasp.oag.session.Session;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that gateway routes are matched most-specific-first, that the route name is exposed to the
 * route filters, and that the filters run in {@code @Order} sequence.
 */
class GatewayRouterFunctionConfigurationTest {

    private static final List<String> executionLog = new ArrayList<>();

    @Test
    void routesTheMostSpecificPathFirstAndRunsFiltersInOrder() throws Exception {

        executionLog.clear();

        var config = configWithRoutes();
        var configuration = new GatewayRouterFunctionConfiguration(config,
                List.of(new SecondFilter(config), new FirstFilter(config)));

        var routerFunction = configuration.gatewayRoutes();

        var servletRequest = new MockHttpServletRequest("GET", "/api/v1/users");
        var request = ServerRequest.create(servletRequest, List.of());

        ScopedValue.where(Session.REQUEST_SESSION, Optional.empty()).
          where(CsrfCookie.REQUEST_CSRF_TOKEN, "CSRF_TOKEN").run(() -> {
              var handlerFunction = routerFunction.route(request);
              assertTrue(handlerFunction.isPresent(), "no route matched /api/v1/users");
            try {
              ((HandlerFunction<ServerResponse>) (handlerFunction.get())).handle(request);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });

        // "first" short-circuits into "second", so seeing both in this order proves the @Order sequence:
        // had "second" run first it would have completed the request and "first" would never appear.
        assertEquals(List.of("first:specific", "second:specific"), executionLog);
    }

    private MainConfig configWithRoutes() throws Exception {

        var config = new TestFileConfigLoader("/localServerConfiguration.yaml").loadConfiguration();

        config.getRoutes().clear();
        config.getRoutes().put("catchAll",
                new GatewayRoute("/api/**", "http://localhost:1", "webapplication", true,
                        PathRewriteConfig.defaultConfig()));
        config.getRoutes().put("specific",
                new GatewayRoute("/api/v1/users", "http://localhost:2", "webapplication", true,
                        PathRewriteConfig.defaultConfig()));

        return config;
    }

    /**
     * Records its own name and the route name it saw. Either completes the request or hands it on,
     * depending on {@code proceed}.
     */
    private static class RecordingFilter extends RouteAwareFilter {

        private final String name;
        private final boolean proceed;

        RecordingFilter(String name, boolean proceed, MainConfig config) {
            this.name = name;
            this.proceed = proceed;
            ReflectionTestUtils.setField(this, "config", config);
        }

        @Override
        public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next,
                                     GatewayRouteContext routeContext) throws Exception {

            executionLog.add(name + ":" + routeContext.getRouteName());
            return proceed ? next.handle(request) : ServerResponse.ok().build();
        }
    }

    /** Runs first and hands the request on. */
    @Order(10)
    private static class FirstFilter extends RecordingFilter {
        FirstFilter(MainConfig config) {
            super("first", true, config);
        }
    }

    /** Runs second and completes the request, so the proxy handler is never reached. */
    @Order(20)
    private static class SecondFilter extends RecordingFilter {
        SecondFilter(MainConfig config) {
            super("second", false, config);
        }
    }
}

package org.owasp.oag.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the gateway routes are ranked after OAG's own controllers and before static resources.
 */
class HandlerMappingOrderConfigurationTest {

    private static final int CONTROLLER_ORDER = 0;
    private static final int STATIC_RESOURCE_ORDER = Ordered.LOWEST_PRECEDENCE - 1;

    private final BeanPostProcessor postProcessor = HandlerMappingOrderConfiguration.routerFunctionMappingAfterControllers();

    @Test
    void ranksTheGatewayRoutesBetweenControllersAndStaticResources() {

        var mapping = new RouterFunctionMapping();
        mapping.setOrder(-1);

        var result = postProcessor.postProcessAfterInitialization(mapping, "routerFunctionMapping");

        assertSame(mapping, result);
        assertTrue(mapping.getOrder() > CONTROLLER_ORDER, "routes must come after the controllers");
        assertTrue(mapping.getOrder() < STATIC_RESOURCE_ORDER, "routes must come before static resources");
    }

    @Test
    void leavesOtherHandlerMappingsUnchanged() {

        var mapping = new RequestMappingHandlerMapping();
        mapping.setOrder(CONTROLLER_ORDER);

        var result = postProcessor.postProcessAfterInitialization(mapping, "requestMappingHandlerMapping");

        assertSame(mapping, result);
        assertEquals(CONTROLLER_ORDER, mapping.getOrder());
    }
}

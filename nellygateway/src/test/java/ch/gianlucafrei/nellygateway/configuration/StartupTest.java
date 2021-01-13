package ch.gianlucafrei.nellygateway.configuration;

import ch.gianlucafrei.nellygateway.NellygatewayApplication;
import ch.gianlucafrei.nellygateway.config.NellyConfigLoader;
import ch.gianlucafrei.nellygateway.config.configuration.NellyConfig;
import ch.gianlucafrei.nellygateway.config.configuration.NellyRoute;
import ch.gianlucafrei.nellygateway.mockserver.TestFileConfigLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
class StartupTest {

    @Autowired
    private NellyConfig nellyConfig;

    @Autowired
    private ApplicationContext context;

    @Test
    void testLoadRoutes() {

        // Arrange (Load configuration)
        NellyRoute local = nellyConfig.getRoutes().get("local");
        ZuulProperties zuulProperties = (ZuulProperties) context.getBean("zuul.CONFIGURATION_PROPERTIES");

        // Act Load routes from zuul context
        Map<String, ZuulProperties.ZuulRoute> routes = zuulProperties.getRoutes();
        ZuulProperties.ZuulRoute localZuulRoute = routes.get("local");

        // Assert
        assertEquals(local.getPath(), localZuulRoute.getPath());
    }

    @Configuration
    @Import(NellygatewayApplication.class)
    public static class TestConfig {

        @Primary
        @Bean
        NellyConfigLoader nellyConfigLoader() {
            return new TestFileConfigLoader("/localServerConfiguration.yaml");
        }
    }
}

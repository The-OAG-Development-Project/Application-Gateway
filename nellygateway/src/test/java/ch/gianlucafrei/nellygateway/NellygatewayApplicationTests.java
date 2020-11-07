package ch.gianlucafrei.nellygateway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class NellygatewayApplicationTests {

    @BeforeAll
    static void loadConfiguration() throws IOException {
        NellygatewayApplication.loadConfiguration();
    }

    @Test
    void contextLoads() {
    }

}

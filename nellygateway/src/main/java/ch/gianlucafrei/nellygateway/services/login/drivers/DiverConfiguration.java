package ch.gianlucafrei.nellygateway.services.login.drivers;

import ch.gianlucafrei.nellygateway.services.oidc.OIDCService;
import ch.gianlucafrei.nellygateway.services.login.drivers.github.GitHubLoginImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiverConfiguration {

    @Autowired
    private ApplicationContext appContext;

    @Bean(name = "github-driver")
    public OIDCService githubDriver(){
        return new GitHubLoginImplementation();
    }

    public OIDCService getDriver(String name){

        if(name == null)
            return new OIDCService();

        OIDCService driver = (OIDCService) appContext.getBean(name);

        if(driver == null)
            throw new RuntimeException("driver not found");

        return driver;
    }
}

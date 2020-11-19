package ch.gianlucafrei.nellygateway.services.oidc.drivers;

import ch.gianlucafrei.nellygateway.services.oidc.OIDCService;
import ch.gianlucafrei.nellygateway.services.oidc.drivers.github.GitHubDriver;
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
        return new GitHubDriver();
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

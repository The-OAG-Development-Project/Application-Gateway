package ch.gianlucafrei.nellygateway.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class NellyMain implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(NellyMain.class);

    @Autowired
    private ApplicationContext context;

    @Override
    public void run(String... args) throws Exception {

        log.info("Nelly started with {} routes", "?");
    }
}
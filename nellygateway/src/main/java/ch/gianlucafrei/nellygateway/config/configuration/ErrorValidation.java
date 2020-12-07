package ch.gianlucafrei.nellygateway.config.configuration;

import org.springframework.context.ApplicationContext;

import java.util.List;

public interface ErrorValidation {

    List<String> getErrors(ApplicationContext context);
}

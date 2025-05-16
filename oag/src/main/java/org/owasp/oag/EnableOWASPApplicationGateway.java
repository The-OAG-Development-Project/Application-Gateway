package org.owasp.oag;

import org.owasp.oag.infrastructure.OAGBeanConfiguration;
import org.owasp.oag.infrastructure.PostConfigBeanConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to enable OWASP Application Gateway functionality in a Spring Boot application.
 * Applying this annotation to a configuration class will import the necessary bean configurations
 * for the gateway to function properly.
 * 
 * This annotation is used at the type level and is available at runtime through reflection.
 */
@Target(value = TYPE)
@Retention(value = RUNTIME)
@Import({OAGBeanConfiguration.class, PostConfigBeanConfiguration.class})
public @interface EnableOWASPApplicationGateway {
}

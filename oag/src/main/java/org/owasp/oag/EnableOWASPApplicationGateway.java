package org.owasp.oag;

import org.owasp.oag.infrastructure.OAGBeanConfiguration;
import org.owasp.oag.infrastructure.PostConfigBeanConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value=TYPE)
@Retention(value=RUNTIME)
@Import({OAGBeanConfiguration.class, PostConfigBeanConfiguration.class})
public @interface EnableOWASPApplicationGateway {
}

package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.http.replier.config.HttpRemoteReplierConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(HttpRemoteReplierConfig.class)
public @interface EnableMQHttpReplier {
}

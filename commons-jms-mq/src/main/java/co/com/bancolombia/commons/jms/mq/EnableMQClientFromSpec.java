package co.com.bancolombia.commons.jms.mq;

import co.com.bancolombia.commons.jms.mq.config.MQAutoconfiguration;
import co.com.bancolombia.commons.jms.mq.config.MQFromSpecConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MQFromSpecConfiguration.class, MQAutoconfiguration.class})
public @interface EnableMQClientFromSpec {
}

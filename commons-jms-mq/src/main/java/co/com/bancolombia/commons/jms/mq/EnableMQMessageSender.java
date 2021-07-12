package co.com.bancolombia.commons.jms.mq;

import co.com.bancolombia.commons.jms.mq.config.MQAutoconfigurationSender;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MQAutoconfigurationSender.class)
public @interface EnableMQMessageSender {
}

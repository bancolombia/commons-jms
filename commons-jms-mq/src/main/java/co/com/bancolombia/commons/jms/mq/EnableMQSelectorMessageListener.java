package co.com.bancolombia.commons.jms.mq;

import co.com.bancolombia.commons.jms.mq.config.MQAutoconfigurationSelectorListener;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MQAutoconfigurationSelectorListener.class)
public @interface EnableMQSelectorMessageListener {
}

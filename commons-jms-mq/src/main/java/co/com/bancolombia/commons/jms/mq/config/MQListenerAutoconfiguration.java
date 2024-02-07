package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.mq.config.factory.MQListenerFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration
@AllArgsConstructor
public class MQListenerAutoconfiguration implements BeanPostProcessor {
    private final MQSpringResolver resolver;
        @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {
        MQListenerFactory.postProcessAfterInitialization(resolver, bean, beanName);
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}

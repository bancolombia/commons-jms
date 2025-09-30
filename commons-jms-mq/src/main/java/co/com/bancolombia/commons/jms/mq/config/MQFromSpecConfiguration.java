package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.model.MQClient;
import co.com.bancolombia.commons.jms.api.model.spec.CommonsJMSSpec;
import co.com.bancolombia.commons.jms.mq.config.client.MQClientImpl;
import co.com.bancolombia.commons.jms.mq.config.factory.MQClientFactory;
import org.springframework.context.annotation.Bean;

public class MQFromSpecConfiguration {

    @Bean
    public MQClient mqClient() {
        return new MQClientImpl();
    }

    @Bean
    public boolean mqClientFromSpec(CommonsJMSSpec spec, MQSpringResolver resolver) {
        MQClientFactory.fromSpec(spec, resolver);
        return true;
    }
}

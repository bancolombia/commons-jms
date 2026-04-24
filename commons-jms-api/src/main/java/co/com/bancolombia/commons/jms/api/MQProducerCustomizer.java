package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSProducer;

public interface MQProducerCustomizer {
    void customize(JMSProducer producer);

    default MQProducerCustomizer andThen(MQProducerCustomizer after) {
        if (after == null) {
            return this;
        }
        return producer -> {
            this.customize(producer);
            after.customize(producer);
        };
    }
}

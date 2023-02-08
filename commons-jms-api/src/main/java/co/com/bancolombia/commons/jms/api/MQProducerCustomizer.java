package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSProducer;

public interface MQProducerCustomizer {
    void customize(JMSProducer producer);
}

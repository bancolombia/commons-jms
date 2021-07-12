package co.com.bancolombia.commons.jms.api;

import javax.jms.JMSProducer;

public interface MQProducerCustomizer {
    void customize(JMSProducer producer);
}

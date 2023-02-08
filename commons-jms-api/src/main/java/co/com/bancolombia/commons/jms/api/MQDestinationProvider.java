package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;
import jakarta.jms.JMSContext;

public interface MQDestinationProvider {
    Destination create(JMSContext context);
}

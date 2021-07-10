package co.com.bancolombia.commons.jms.api;

import javax.jms.Destination;
import javax.jms.JMSContext;

public interface MQDestinationProvider {
    Destination create(JMSContext context);
}

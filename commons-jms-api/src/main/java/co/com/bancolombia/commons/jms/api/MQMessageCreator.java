package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

public interface MQMessageCreator {
    Message create(JMSContext context) throws JMSException;
}

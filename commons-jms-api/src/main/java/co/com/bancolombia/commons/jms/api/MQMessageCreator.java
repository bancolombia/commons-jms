package co.com.bancolombia.commons.jms.api;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

public interface MQMessageCreator {
    Message create(JMSContext context) throws JMSException;
}

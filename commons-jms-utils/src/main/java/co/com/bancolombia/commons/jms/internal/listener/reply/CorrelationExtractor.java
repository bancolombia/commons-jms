package co.com.bancolombia.commons.jms.internal.listener.reply;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

public interface CorrelationExtractor {
    String getCorrelationValue(Message message) throws JMSException;
}

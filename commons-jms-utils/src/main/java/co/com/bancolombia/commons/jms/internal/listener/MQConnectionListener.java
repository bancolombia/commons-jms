package co.com.bancolombia.commons.jms.internal.listener;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Builder
@AllArgsConstructor
public class MQConnectionListener implements Runnable {
    private final Session session;
    private final TemporaryQueue destination;
    private final MessageListener listener;
    private final int sequence;

    @Override
    public void run() {
        try {
            Thread.currentThread().setName("mq-listener-temporary-queue-" + sequence + "[" + shortDestinationName() + "]");
            session.createConsumer(destination)//NOSONAR
                    .setMessageListener(listener);
        } catch (JMSRuntimeException e) {
            log.warn("JMSRuntimeException in MQConnectionListener", e);
            throw e;
        } catch (JMSException ex) {
            log.warn("JMSException in MQConnectionListener", ex);
            throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
        }
    }

    private String shortDestinationName() throws JMSException {
        return destination.getQueueName().split("\\?")[0];
    }

}

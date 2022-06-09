package co.com.bancolombia.commons.jms.internal.listener;

import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

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
        } catch (JMSException ex) {
            throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
        }
    }

    private String shortDestinationName() throws JMSException {
        return destination.getQueueName().split("\\?")[0];
    }

}

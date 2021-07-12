package co.com.bancolombia.commons.jms.internal.listener;

import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.jms.*;

@Builder
@AllArgsConstructor
public class MQConnectionListener implements Runnable {
    private final Session session;
    private final TemporaryQueue destination;
    private final MessageListener listener;

    @Override
    public void run() {
        try {
            session.createConsumer(destination)//NOSONAR
                    .setMessageListener(listener);
        } catch (JMSException ex) {
            throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
        }
    }

}

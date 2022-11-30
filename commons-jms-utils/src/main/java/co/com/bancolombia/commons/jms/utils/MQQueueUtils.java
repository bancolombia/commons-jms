package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQQueueUtils {

    public static Destination setupFixedQueue(JMSContext context, MQListenerConfig config) {
        Queue queue = context.createQueue(config.getQueue());
        customize(queue, config);
        if (config.getQmSetter() != null) {
            config.getQmSetter().accept(context, queue);
        }
        return queue;
    }

    public static TemporaryQueue setupTemporaryQueue(Session session, MQListenerConfig config) {
        try {
            TemporaryQueue queue = session.createTemporaryQueue();
            customize(queue, config);
            return queue;
        } catch (JMSException ex) {
            throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
        }
    }

    private static <T extends Queue> void customize(T queue, MQListenerConfig config) {
        if (config.getCustomizer() != null) {
            try {
                config.getCustomizer().customize(queue);
            } catch (JMSException ex) {
                throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
            }
        }
    }
}

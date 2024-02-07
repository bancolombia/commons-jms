package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.jms.TemporaryQueue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQQueueUtils {

    public static Destination setupFixedQueue(JMSContext context, MQListenerConfig config) {
        Queue queue = context.createQueue(config.getListeningQueue());
        customize(queue, config);
        if (config.getQmSetter() != null) {
            config.getQmSetter().accept(context, queue);
        }
        return queue;
    }

    public static TemporaryQueue setupTemporaryQueue(JMSContext context, MQListenerConfig config) {
        TemporaryQueue queue = context.createTemporaryQueue();
        customize(queue, config);
        return queue;
    }

    private static <T extends Queue> void customize(T queue, MQListenerConfig config) {
        if (config.getQueueCustomizer() != null) {
            try {
                config.getQueueCustomizer().customize(queue);
            } catch (Exception ex) {
                log.warn("Error customizing queue", ex);
            }
        }
    }
}

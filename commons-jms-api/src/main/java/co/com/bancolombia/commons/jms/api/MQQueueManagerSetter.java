package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import java.util.function.BiConsumer;

public interface MQQueueManagerSetter extends BiConsumer<JMSContext, Queue> {
}

package co.com.bancolombia.commons.jms.api;

import javax.jms.JMSContext;
import javax.jms.Queue;
import java.util.function.BiConsumer;

public interface MQQueueManagerSetter extends BiConsumer<JMSContext, Queue> {
}

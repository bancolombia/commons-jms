package co.com.bancolombia.commons.jms.mq.utils;


import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsReadablePropertyContext;
import lombok.extern.log4j.Log4j2;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import java.lang.reflect.Field;

import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_QUEUE_MANAGER;

@Log4j2
public class MQUtils {
    private static final String CONNECTION_PROPERTY = "connection";

    public static String extractQMName(JMSContext context) {
        try {
            Field field = context.getClass().getDeclaredField(CONNECTION_PROPERTY);
            field.setAccessible(true);
            JmsReadablePropertyContext readable = (JmsReadablePropertyContext) field.get(context);
            return readable.getStringProperty(WMQ_QUEUE_MANAGER);
        } catch (NoSuchFieldException | JMSException | IllegalAccessException e) {
            log.warn("Error getting queue manager name from JMSContext", e);
        }
        return "";
    }

    public static void setQMName(Queue queue, String qmName) {
        try {
            ((MQQueue) queue).setBaseQueueManagerName(qmName);
        } catch (JMSException e) {
            log.warn("Error setting queue manager name to queue", e);
        }
    }
}

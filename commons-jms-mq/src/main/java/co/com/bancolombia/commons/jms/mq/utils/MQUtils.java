package co.com.bancolombia.commons.jms.mq.utils;


import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.jms.JmsReadablePropertyContext;
import com.ibm.msg.client.jakarta.wmq.common.internal.WMQUtils;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TemporaryQueue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.UUID;

import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_MQMD_CORRELID;
import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMS_IBM_MQMD_MSGID;
import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_RESOLVED_QUEUE_MANAGER;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQUtils {
    private static final String CONNECTION_PROPERTY = "connection";

    public static String extractQMNameWithTempQueue(JMSContext context) {
        try {
            TemporaryQueue queue = context.createTemporaryQueue();
            String qmName = queue.toString().split("/")[2];
            log.info("Listening queue manager name got successfully: {}", qmName);
            queue.delete();
            return qmName;
        } catch (Exception e) {
            log.info("Queue manager name could not be got through a temporary queue", e);
            return "";
        }
    }

    public static String extractQMName(JMSContext context) {
        try {
            Field field = context.getClass().getDeclaredField(CONNECTION_PROPERTY);
            AccessibleObject.setAccessible(new AccessibleObject[]{field}, true);//NOSONAR
            JmsReadablePropertyContext readable = (JmsReadablePropertyContext) field.get(context);
            AccessibleObject.setAccessible(new AccessibleObject[]{field}, false);
            String qmName = readable.getStringProperty(WMQ_RESOLVED_QUEUE_MANAGER);
            log.info("Listening queue manager name got successfully: {}", qmName);
            return qmName;
        } catch (NoSuchFieldException | JMSException | IllegalAccessException e) {
            log.info("Queue manager name could not be got from the JMSContext", e);
            return "";
        }
    }

    public static void setQMName(Queue queue, String qmName) {
        try {
            ((MQQueue) queue).setBaseQueueManagerName(qmName);
        } catch (JMSException e) {
            log.info("Error setting queue manager name to queue", e);
        }
    }

    public static void setQMNameIfNotSet(JMSContext context, Queue queue) {
        setQMNameIfNotSet(context, queue, false);
    }

    public static void setQMNameIfNotSet(JMSContext context, Queue queue, boolean useTemporaryQueue) {
        if (((MQQueue) queue).getBaseQueueManagerName() == null) {
            String qmName = useTemporaryQueue ? extractQMNameWithTempQueue(context) : extractQMName(context);
            setQMName(queue, qmName);
        }
    }

    @SneakyThrows
    public static void setMessageId(Message message, String id) {
        message.setObjectProperty(JMS_IBM_MQMD_MSGID, WMQUtils.stringToId(id));
    }

    @SneakyThrows
    public static void setCorrelationId(Message message, String id) {
        message.setObjectProperty(JMS_IBM_MQMD_CORRELID, WMQUtils.stringToId(id));
    }

    public static String generateUniqueId() {
        var input = UUID.randomUUID().toString().replace("-", "");
        var bytes = input.getBytes();
        var hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return "ID:" + hexString.substring(0, 48);
    }
}

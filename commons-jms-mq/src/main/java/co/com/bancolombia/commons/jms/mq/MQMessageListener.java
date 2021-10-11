package co.com.bancolombia.commons.jms.mq;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.lang.reflect.Method;

import static com.ibm.msg.client.jms.JmsConstants.JMSX_DELIVERY_COUNT;

@Log4j2
@AllArgsConstructor
public final class MQMessageListener implements MessageListener {
    private final InvocableHandlerMethod method;
    private final int maxRetries;

    public static MQMessageListener fromBeanAndMethod(Object bean, Method invocableMethod, int retries) {
        InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(bean, invocableMethod);
        return new MQMessageListener(handlerMethod, retries);
    }

    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        callRealMethod(message);
    }

    private void callRealMethod(Message message) throws Exception {
        try {
            method.invoke(MessageBuilder.createMessage("", new MessageHeaders(null)), message);
        } catch (Exception error) {
            if (maxRetries != -1 && maxRetries < message.getIntProperty(JMSX_DELIVERY_COUNT)) {
                log.warn("Discarding message {} after {} retries", message.getJMSMessageID(), maxRetries);
                log.warn("Cause", error);
            } else {
                log.warn("Message {} will be retried", message.getJMSMessageID());
                log.warn("Cause", error);
                throw error;
            }
        }
    }
}

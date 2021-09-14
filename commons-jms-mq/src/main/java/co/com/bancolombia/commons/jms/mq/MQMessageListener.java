package co.com.bancolombia.commons.jms.mq;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.lang.reflect.Method;

@AllArgsConstructor
public final class MQMessageListener implements MessageListener {
    private final InvocableHandlerMethod method;

    public static MQMessageListener fromBeanAndMethod(Object bean, Method invocableMethod) {
        InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(bean, invocableMethod);
        return new MQMessageListener(handlerMethod);
    }

    @SneakyThrows
    @Override
    public void onMessage(Message message) {
        callRealMethod(message);
    }

    private void callRealMethod(Message message) throws Exception {
        method.invoke(MessageBuilder.createMessage("", new MessageHeaders(null)), message);
    }
}

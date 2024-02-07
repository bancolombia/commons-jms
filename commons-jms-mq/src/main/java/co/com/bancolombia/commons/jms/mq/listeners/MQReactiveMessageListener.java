package co.com.bancolombia.commons.jms.mq.listeners;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Log4j2
public final class MQReactiveMessageListener extends MQMessageListenerRetries implements MessageListener {
    private final InvocableHandlerMethod method;

    public MQReactiveMessageListener(InvocableHandlerMethod method, int maxRetries) {
        super(maxRetries);
        this.method = method;
    }

    public static MQReactiveMessageListener fromBeanAndMethod(Object bean, Method invocableMethod, int retries) {
        InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(bean, invocableMethod);
        return new MQReactiveMessageListener(handlerMethod, retries);
    }

    protected Mono<Object> process(Message message) {
        return method.invoke(MessageBuilder.createMessage("", new MessageHeaders(null)), message);
    }
}

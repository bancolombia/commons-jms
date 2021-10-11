package co.com.bancolombia.commons.jms.mq;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.lang.reflect.Method;

import static com.ibm.msg.client.jms.JmsConstants.JMSX_DELIVERY_COUNT;

@Log4j2
@AllArgsConstructor
public final class MQReactiveMessageListener implements MessageListener {
    private final InvocableHandlerMethod method;
    private final int maxRetries;

    public static MQReactiveMessageListener fromBeanAndMethod(Object bean, Method invocableMethod, int retries) {
        InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(bean, invocableMethod);
        return new MQReactiveMessageListener(handlerMethod, retries);
    }

    @Override
    public void onMessage(Message message) {
        onMessageAsync(message).toFuture().join();
    }

    @SneakyThrows
    private Mono<Object> onMessageAsync(Message message) {
        Mono<Object> flow = Mono.defer(() -> callRealMethod(message));
        if (maxRetries != -1 && maxRetries < message.getIntProperty(JMSX_DELIVERY_COUNT)) {
            flow = flow.onErrorResume(e -> discardMessage(message, e));
        } else {
            flow = flow.doOnError(error -> logRetry(message, error));
        }
        return flow.subscribeOn(Schedulers.boundedElastic());
    }

    @SneakyThrows
    private Mono<Object> discardMessage(Message message, Throwable error) {
        log.warn("Discarding message {} after {} retries", message.getJMSMessageID(), maxRetries);
        log.warn("Cause", error);
        return Mono.empty();
    }

    @SneakyThrows
    private void logRetry(Message message, Throwable error) {
        log.warn("Message {} will be retried", message.getJMSMessageID());
        log.warn("Cause", error);
    }

    private Mono<Object> callRealMethod(Message message) {
        return method.invoke(MessageBuilder.createMessage("", new MessageHeaders(null)), message);
    }
}

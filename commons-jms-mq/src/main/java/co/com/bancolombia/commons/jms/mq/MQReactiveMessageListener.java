package co.com.bancolombia.commons.jms.mq;

import lombok.AllArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.jms.Message;
import javax.jms.MessageListener;

@AllArgsConstructor
public final class MQReactiveMessageListener implements MessageListener {
    private final InvocableHandlerMethod method;

    @Override
    public void onMessage(Message message) {
        onMessageAsync(message)
                .subscribe();
    }

    protected Mono<Object> onMessageAsync(Message message) {
        return Mono.defer(() -> callRealMethod(message))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Object> callRealMethod(Message message) {
        return method.invoke(MessageBuilder.createMessage("", new MessageHeaders(null)), message);
    }
}

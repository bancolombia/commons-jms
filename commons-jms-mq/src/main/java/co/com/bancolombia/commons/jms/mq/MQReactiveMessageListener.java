package co.com.bancolombia.commons.jms.mq;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.jms.Message;
import javax.jms.MessageListener;

@AllArgsConstructor
public final class MQReactiveMessageListener implements MessageListener {
    private final InvocableHandlerMethod method;

    @Override
    public void onMessage(Message message) {
        Mono.defer(() -> process(message))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    protected Mono<Object> process(Message message) {
        return method.invoke(null, message);
    }
}

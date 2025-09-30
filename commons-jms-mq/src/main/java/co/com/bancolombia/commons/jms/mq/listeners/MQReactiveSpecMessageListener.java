package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.model.spec.MQMessageListenerSpec;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public final class MQReactiveSpecMessageListener extends MQMessageListenerRetries implements MessageListener {
    private final MQMessageListenerSpec spec;

    public MQReactiveSpecMessageListener(MQMessageListenerSpec spec, int maxRetries) {
        super(maxRetries);
        this.spec = spec;
    }

    protected Mono<Object> process(Message message) {
        return spec.getHandler().handleMessage(spec.getQueueName(), message);
    }
}

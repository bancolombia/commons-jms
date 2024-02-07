package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQDomainMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import jakarta.jms.Destination;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.TreeMap;

@AllArgsConstructor
public class MQDomainContextSender implements MQDomainMessageSender {
    private final TreeMap<String, MQMessageSender> senders; // <domain, MQMultiContextSender>

    @Override
    public Mono<String> send(String domain, Destination destination, MQMessageCreator messageCreator) {
        return senders.get(domain).send(destination, messageCreator);
    }

    @Override
    public Mono<String> send(String domain, MQMessageCreator messageCreator) {
        return senders.get(domain).send(messageCreator);
    }

    @Override
    public MQMessageSender forDomain(String domain) {
        return senders.get(domain);
    }
}

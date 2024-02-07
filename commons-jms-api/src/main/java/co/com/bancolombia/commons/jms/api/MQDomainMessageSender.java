package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;
import reactor.core.publisher.Mono;

public interface MQDomainMessageSender {
    Mono<String> send(String domain, Destination destination, MQMessageCreator messageCreator);

    Mono<String> send(String domain, MQMessageCreator messageCreator);

    /**
     * You can retrieve the MQMessageSender to avoid queries to Map
     * @param domain Domain name or connectionFactory bean name
     * @return MQMessageSender
     */
    MQMessageSender forDomain(String domain);
}

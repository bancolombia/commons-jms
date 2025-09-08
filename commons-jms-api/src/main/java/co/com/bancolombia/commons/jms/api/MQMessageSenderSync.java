package co.com.bancolombia.commons.jms.api;

import jakarta.jms.Destination;

public interface MQMessageSenderSync {
    String send(String message);

    String send(MQMessageCreator messageCreator);

    String send(String destination, String message);

    String send(String destination, MQMessageCreator messageCreator);

    String send(Destination destination, String message);

    String send(Destination destination, MQMessageCreator messageCreator);
}

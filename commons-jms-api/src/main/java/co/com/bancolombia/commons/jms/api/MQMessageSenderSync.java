package co.com.bancolombia.commons.jms.api;

import javax.jms.Destination;

public interface MQMessageSenderSync {
    String send(Destination destination, MQMessageCreator messageCreator);

    String send(MQMessageCreator messageCreator);
}

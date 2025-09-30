package co.com.bancolombia.commons.jms.api.model.spec;

import co.com.bancolombia.commons.jms.api.model.MQMessageHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MQMessageListenerSpec {
    private final String queueName;
    private final MQMessageHandler handler;
}

package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;

import javax.jms.*;

public class MQContextSenderSync implements MQMessageSenderSync {
    private final JMSContext context;
    private final JMSProducer producer;
    private final Destination defaultDestination;

    public MQContextSenderSync(JMSContext context, MQDestinationProvider provider, MQProducerCustomizer customizer) {
        this.context = context;
        this.producer = context.createProducer();
        customizer.customize(producer);
        this.defaultDestination = provider.create(context);
    }

    @Override
    public String send(MQMessageCreator messageCreator) {
        return send(defaultDestination, messageCreator);
    }

    @Override
    public String send(Destination destination, MQMessageCreator messageCreator) {
        try {
            Message message = messageCreator.create(context);
            producer.send(destination, message);
            return message.getJMSMessageID();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}

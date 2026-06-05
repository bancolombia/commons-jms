package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.internal.models.MQSenderConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuperBuilder
public class MQContextSenderSync extends AbstractJMSReconnectable<MQContextSenderSync> implements MQMessageSenderSync {
    private final MQSenderConfig senderConfig;
    private JMSProducer producer;
    private Destination defaultDestination;

    @Override
    protected String name() {
        String[] parts = this.toString().split("\\.");
        return parts[parts.length - 1];
    }

    @Override
    protected MQContextSenderSync self() {
        return this;
    }

    @Override
    protected void connect() {
        log.info("Starting sender {}", getProcess());
        this.context = senderConfig.getConnectionFactory().createContext();
        this.context.setExceptionListener(this);
        this.producer = context.createProducer();
        senderConfig.getProducerCustomizer().customize(producer);
        this.defaultDestination = senderConfig.getDestinationProvider().create(context);
        log.info("Sender {} started successfully", getProcess());
    }

    @Override
    public String send(String message) {
        return send(createMessageCreator(message));
    }

    @Override
    public String send(MQMessageCreator messageCreator) {
        return send(defaultDestination, messageCreator);
    }

    @Override
    public String send(String destination, String message) {
        return send(createDestination(destination), createMessageCreator(message));
    }

    @Override
    public String send(String destination, MQMessageCreator messageCreator) {
        return send(createDestination(destination), messageCreator);
    }

    @Override
    public String send(Destination destination, String message) {
        return send(destination, createMessageCreator(message));
    }

    @Override
    public String send(Destination destination, MQMessageCreator messageCreator) {
        return send(destination, messageCreator, true);
    }

    private String send(Destination destination, MQMessageCreator messageCreator, boolean retry) {
        try {
            Message message = messageCreator.create(context);
            producer.send(destination, message);
            return message.getJMSMessageID();
        } catch (JMSException e) {
            if (retry) {
                return send(destination, messageCreator, false);
            } else {
                log.warn("JMSException in MQContextSenderSync", e);
                throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
            }
        } catch (JMSRuntimeException ex) {
            if (retry) {
                log.warn("Retrying because: {}", ex.getMessage());
                // Connection is broken, try recover before retry
                if (isReconnectable(ex)) {
                    start();
                }
                return send(destination, messageCreator, false);
            }
            onException(ex); // Handle for reconnection
            throw ex;
        }
    }

    private Destination createDestination(String destination) {
        return this.context.createQueue(destination);
    }

    private MQMessageCreator createMessageCreator(String message) {
        return ctx -> ctx.createTextMessage(message);
    }
}

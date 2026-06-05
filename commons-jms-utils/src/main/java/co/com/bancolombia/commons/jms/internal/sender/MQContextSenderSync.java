package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.internal.models.MQSenderConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
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

    private JMSContext context;
    private JMSProducer producer;
    private Destination defaultDestination;

    @Override
    protected String name() {
        String[] parts = this.toString().split("\\.");
        return parts[parts.length - 1];
    }

    @Override
    protected void disconnect() {
        // do not disconnect to avoid another thread exceptions
    }

    @Override
    protected MQContextSenderSync connect() {
        long handled = System.currentTimeMillis();
        synchronized (this) {
            if (handled > lastSuccess.get()) {
                log.info("Starting sender {}", getProcess());
                this.context = senderConfig.getConnectionFactory().createContext();
                this.context.setExceptionListener(this);
                this.producer = context.createProducer();
                senderConfig.getProducerCustomizer().customize(producer);
                this.defaultDestination = senderConfig.getDestinationProvider().create(context);
                log.info("Sender {} started successfully", getProcess());
                lastSuccess.set(System.currentTimeMillis());
            } else {
                log.warn("Reconnection ignored because already connected");
            }
        }
        return this;
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
            log.warn("JMSRuntimeException in MQContextSenderSync", ex);
            if ("JMSCC0008".equals(ex.getErrorCode()) || (ex.getCause() != null
                    && ex.getCause().getMessage().contains("CONNECTION_BROKEN"))) { // Connection is closed
                if (retry) {
                    log.warn("Retrying send");
                    connect();
                    return send(destination, messageCreator, false);
                } else {
                    onException(ex); // Handle for reconnection
                }
            }
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

package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.reconnect.AbstractJMSReconnectable;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MessageListener;
import jakarta.jms.TemporaryQueue;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SuperBuilder
public class MQMultiConnectionListener extends AbstractJMSReconnectable<MQMultiConnectionListener> {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQQueuesContainer container;
    private final MQListenerConfig config;
    private ExecutorService service;
    private Connection connection;

    @Override
    protected String name() {
        return "mq-lister-temporary-queue-[" + config.getTempQueueAlias() + "]";
    }


    @Override
    protected void disconnect() throws JMSException {
        if (service != null && !service.isTerminated() && !service.isShutdown()) {
            service.shutdown();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    @SuppressWarnings("resource")
    protected MQMultiConnectionListener connect() {
        log.info("Starting listener {}", getProcess());
        try {
            connection = connectionFactory.createConnection();//NOSONAR
            connection.setExceptionListener(this);
            TemporaryQueue destination = MQQueueUtils.setupTemporaryQueue(connection.createSession(), config);
            container.registerQueue(config.getTempQueueAlias(), destination);

            service = Executors.newFixedThreadPool(config.getConcurrency());
            for (int i = 0; i < config.getConcurrency(); i++) {
                service.submit(MQConnectionListener.builder()
                        .session(connection.createSession())
                        .destination(destination)
                        .listener(listener)
                        .sequence(i)
                        .build());
            }
            connection.start();
            if (log.isInfoEnabled()) {
                log.info("{} listeners created for {} with queue {}", config.getConcurrency(), getProcess(), destination.getQueueName());
            }
        } catch (JMSException ex) {
            throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
        }
        return this;
    }
}

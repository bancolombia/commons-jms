package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageListener;
import javax.jms.TemporaryQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Builder
@Log4j2
public class MQMultiConnectionListener {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQQueuesContainer container;
    private final MQListenerConfig config;

    public void start() {
        ExecutorService service = Executors.newFixedThreadPool(config.getConcurrency());
        try {
            Connection connection = connectionFactory.createConnection();//NOSONAR
            TemporaryQueue destination = MQQueueUtils.setupTemporaryQueue(connection.createSession(), config);
            container.registerQueue(config.getTempQueueAlias(), destination);
            for (int i = 0; i < config.getConcurrency(); i++) {
                service.submit(MQConnectionListener.builder()
                        .session(connection.createSession())
                        .destination(destination)
                        .listener(listener)
                        .build());
            }
            connection.start();
            if (log.isInfoEnabled()) {
                log.info("{} listeners created for {}", config.getConcurrency(), destination.getQueueName());
            }
        } catch (JMSException ex) {
            throw new JMSRuntimeException(ex.getMessage(), ex.getErrorCode(), ex);
        }
    }
}

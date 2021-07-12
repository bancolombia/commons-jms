package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueueUtils;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

import javax.jms.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Builder
@Log4j2
public class MQMultiConnectionListener {
    private final ConnectionFactory connectionFactory;
    private final MessageListener listener;
    private final MQTemporaryQueuesContainer container;
    private final MQListenerConfig config;

    public void start() {
        ExecutorService service = Executors.newFixedThreadPool(config.getConcurrency());
        try {
            Connection connection = connectionFactory.createConnection();//NOSONAR
            TemporaryQueue destination = MQQueueUtils.setupTemporaryQueue(connection.createSession(), config);
            container.registerTemporaryQueue(config.getTempQueueAlias(), destination);
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

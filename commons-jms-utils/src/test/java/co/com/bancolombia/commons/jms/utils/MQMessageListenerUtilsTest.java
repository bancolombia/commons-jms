package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MessageListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static co.com.bancolombia.commons.jms.internal.models.MQListenerConfig.QueueType.TEMPORARY;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MQMessageListenerUtilsTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MessageListener listener;
    @Mock
    private MQQueuesContainer container;
    @Mock
    private MQBrokerUtils utils;
    @Mock
    private MQHealthListener healthListener;

    @Test
    void shouldCreateFixedQueueListeners() {
        // Arrange
        MQListenerConfig config = MQListenerConfig.builder()
                .listeningQueue("QUEUE.NAME")
                .concurrency(5)
                .messageListener(listener)
                .connectionFactory(connectionFactory)
                .build();
        RetryableConfig retryableConfig = RetryableConfig
                .builder()
                .maxRetries(10)
                .initialRetryIntervalMillis(1000)
                .multiplier(1.5)
                .build();
        // Act
        MQMessageListenerUtils.createListeners(config, container, utils, healthListener, retryableConfig);
    }

    @Test
    void shouldCreateTemporaryQueueListeners() throws JMSException {
        // Arrange
        MQListenerConfig config = spy(MQListenerConfig.builder()
                .listeningQueue("alias")
                .connectionFactory(connectionFactory)
                .queueType(TEMPORARY)
                .concurrency(5)
                .build());
        RetryableConfig retryableConfig = RetryableConfig
                .builder()
                .maxRetries(10)
                .initialRetryIntervalMillis(1000)
                .multiplier(1.5)
                .build();
        // Act
        MQMessageListenerUtils.createListeners(config, container, utils, healthListener, retryableConfig);
        // Assert
        verify(config, atLeastOnce()).getConcurrency();
    }
}

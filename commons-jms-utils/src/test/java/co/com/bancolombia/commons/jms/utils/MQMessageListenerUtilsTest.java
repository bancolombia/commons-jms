package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private TemporaryQueue queue;

    @Test
    void shouldCreateFixedQueueListeners() {
        // Arrange
        MQListenerConfig config = MQListenerConfig.builder()
                .queue("QUEUE.NAME")
                .concurrency(5)
                .build();
        // Act
        MQMessageListenerUtils.createListeners(connectionFactory, listener, container, utils, config);
    }

    @Test
    void shouldCreateTemporaryQueueListeners() throws JMSException {
        // Arrange
        MQListenerConfig config = MQListenerConfig.builder()
                .tempQueueAlias("alias")
                .concurrency(5)
                .build();
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession()).thenReturn(session);
        when(session.createTemporaryQueue()).thenReturn(queue);
        // Act
        MQMessageListenerUtils.createListeners(connectionFactory, listener, container, utils, config);
        // Assert
        verify(connectionFactory, times(1)).createConnection();
    }
}

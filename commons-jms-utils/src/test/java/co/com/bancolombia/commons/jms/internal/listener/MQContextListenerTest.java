package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.MessageListener;
import jakarta.jms.Queue;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQContextListenerTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MessageListener listener;
    @Mock
    private JMSContext context;
    @Mock
    private MQBrokerUtils utils;
    @Mock
    private Queue queue;
    @Mock
    private JMSConsumer consumer;
    @Mock
    private MQHealthListener healthListener;
    private MQContextListener contextListener;

    @BeforeEach
    void setup() {
        contextListener = MQContextListener.builder()
                .config(MQListenerConfig.builder().queue("QUEUE.NAME").build())
                .listener(listener)
                .connectionFactory(connectionFactory)
                .container(new MQQueuesContainerImp())
                .healthListener(healthListener)
                .utils(utils)
                .build();
    }

    @Test
    void shouldStartListener() {
        // Arrange
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        when(context.createConsumer(queue)).thenReturn(consumer);
        // Act
        contextListener.call();
        // Assert
        verify(consumer, times(1)).setMessageListener(listener);
    }
}

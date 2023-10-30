package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MessageListener;
import jakarta.jms.TemporaryQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQContextTemporaryListenerTest {
    @Mock
    private MessageListener listener;
    @Mock
    private JMSContext context;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private MQHealthListener healthListener;

    @Mock
    private TemporaryQueue tmpQueue;
    @Mock
    private JMSConsumer consumer;

    private MQContextListener contextTemporaryListener;

    @BeforeEach
    void setup() throws JMSException {
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createTemporaryQueue()).thenReturn(tmpQueue);
        when(context.createConsumer(any())).thenReturn(consumer);
        when(tmpQueue.getQueueName()).thenReturn("AMQ.QUEUE");
        contextTemporaryListener = MQContextListener.builder()
                .listener(listener)
                .temporary(true)
                .connectionFactory(connectionFactory)
                .container(new MQQueuesContainerImp())
                .healthListener(healthListener)
                .config(MQListenerConfig.builder().build())
                .build();
    }

    @Test
    void shouldStartListener() throws JMSException {
        // Arrange
        // Act
        contextTemporaryListener.call();
        // Assert
        verify(consumer, times(1)).setMessageListener(listener);
    }

    @Test
    void shouldDisconnect() throws JMSException {
        // Arrange
        contextTemporaryListener.call();
        // Act
        contextTemporaryListener.disconnect();
        // Assert
        verify(context, times(1)).close();
    }

}

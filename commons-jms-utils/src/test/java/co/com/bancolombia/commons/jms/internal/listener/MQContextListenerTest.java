package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MQContextListenerTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MessageListener listener;
    @Mock
    private JMSContext context;
    @Mock
    private Queue queue;
    @Mock
    private JMSConsumer consumer;
    private MQContextListener contextListener;

    @BeforeEach
    void setup() {
        contextListener = MQContextListener.builder()
                .config(MQListenerConfig.builder().queue("QUEUE.NAME").build())
                .listener(listener)
                .connectionFactory(connectionFactory)
                .build();
    }

    @Test
    void shouldStartListener() {
        // Arrange
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        when(context.createConsumer(queue)).thenReturn(consumer);
        // Act
        contextListener.run();
        // Assert
        verify(consumer, times(1)).setMessageListener(listener);
    }
}

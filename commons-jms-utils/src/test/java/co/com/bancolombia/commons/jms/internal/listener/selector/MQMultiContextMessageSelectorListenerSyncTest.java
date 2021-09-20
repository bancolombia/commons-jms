package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.*;
import java.util.UUID;

import static co.com.bancolombia.commons.jms.internal.listener.selector.MQContextMessageSelectorListenerSync.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MQMultiContextMessageSelectorListenerSyncTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private JMSContext context;
    @Mock
    private JMSConsumer consumer;
    @Mock
    private Queue queue;
    @Mock
    private TextMessage message;

    private MQMessageSelectorListenerSync listenerSync;

    @BeforeEach
    void setup() {
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        MQListenerConfig config = MQListenerConfig
                .builder()
                .concurrency(1)
                .queue("QUEUE")
                .build();
        listenerSync = new MQMultiContextMessageSelectorListenerSync(connectionFactory, config);
    }

    @Test
    void shouldGetMessage() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Message receivedMessage = listenerSync.getMessage(messageID);
        // Assert
        assertEquals(message, receivedMessage);
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldHandleTimeoutErrorWithCustomTimeout() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(null);
        // Act
        // Assert
        assertThrows(ReceiveTimeoutException.class, () -> listenerSync.getMessage(messageID, DEFAULT_TIMEOUT, queue));
    }

}

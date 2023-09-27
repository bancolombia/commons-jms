package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.jms.*;
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
    @Mock
    private MQHealthListener healthListener;

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
        RetryableConfig retryableConfig = RetryableConfig
                .builder()
                .maxRetries(10)
                .initialRetryIntervalMillis(1000)
                .multiplier(1.5)
                .build();
        listenerSync = new MQMultiContextMessageSelectorListenerSync(connectionFactory, config, healthListener, retryableConfig);
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
    void shouldGetMessageWithTimeout() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Message receivedMessage = listenerSync.getMessage(messageID, DEFAULT_TIMEOUT);
        // Assert
        assertEquals(message, receivedMessage);
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldGetMessageBySelector() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Message receivedMessage = listenerSync.getMessageBySelector("JMSMessageID='" + messageID + "'");
        // Assert
        assertEquals(message, receivedMessage);
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldGetMessageBySelectorWithTimeout() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Message receivedMessage = listenerSync.getMessageBySelector("JMSMessageID='" + messageID + "'", DEFAULT_TIMEOUT);
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

    @Test
    void shouldHandleTimeoutErrorWithCustomTimeoutBySelector() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(null);
        // Act
        // Assert
        assertThrows(ReceiveTimeoutException.class, () -> listenerSync.getMessageBySelector("JMSMessageID='" + messageID + "'", DEFAULT_TIMEOUT, queue));
    }

}

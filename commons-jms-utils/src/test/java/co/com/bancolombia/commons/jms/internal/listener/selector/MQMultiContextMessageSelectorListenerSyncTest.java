package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.ContextPerMessageStrategy;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static co.com.bancolombia.commons.jms.internal.listener.selector.MQContextMessageSelectorListenerSync.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
    @Mock
    private Destination destination;
    @Mock
    private SelectorModeProvider selectorModeProvider;
    @Mock
    private MQQueuesContainer container;

    private MQMultiContextMessageSelectorListenerSync listenerSync;

    private MQListenerConfig config;
    private final RetryableConfig retryableConfig = RetryableConfig
            .builder()
            .maxRetries(10)
            .initialRetryIntervalMillis(1000)
            .multiplier(1.5)
            .build();

    @BeforeEach
    void setup() {
        when(selectorModeProvider.get(any(), any()))
                .thenReturn(SelectorModeProvider.defaultSelector().get(connectionFactory, context));
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        config = MQListenerConfig
                .builder()
                .concurrency(1)
                .connectionFactory(connectionFactory)
                .listeningQueue("QUEUE")
                .build();
        listenerSync = new MQMultiContextMessageSelectorListenerSync(config, healthListener,
                retryableConfig, selectorModeProvider, container);
    }

    @Test
    void shouldGetMessageWithContextPerMessage() {
        // Arrange
        listenerSync = new MQMultiContextMessageSelectorListenerSync(config, healthListener,
                retryableConfig, (factory, ignored) -> new ContextPerMessageStrategy(factory), container);
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
    void shouldGetMessageWithContextPerMessageWithErrorTimeout() {
        // Arrange
        listenerSync = new MQMultiContextMessageSelectorListenerSync(config, healthListener,
                retryableConfig, (factory, ignored) -> new ContextPerMessageStrategy(factory), container);
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(null);
        // Assert
        assertThrows(ReceiveTimeoutException.class, () -> {
            // Act
            listenerSync.getMessage(messageID);
        });
    }

    @Test
    void shouldGetMessage() {
        // Arrange
        listenerSync = new MQMultiContextMessageSelectorListenerSync(config, healthListener,
                retryableConfig, SelectorModeProvider.defaultSelector(), container);
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
    void shouldGetMessageBySelectorWithTimeoutError() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(null);
        // Assert
        assertThrows(ReceiveTimeoutException.class, () -> {
            // Act
            listenerSync.getMessageBySelector("JMSMessageID='" + messageID + "'", DEFAULT_TIMEOUT);
        });
    }

    @Test
    void shouldRetrySelectMessageById() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenThrow(new JMSRuntimeException("")).thenReturn(message);
        // Act
        Message receivedMessage = listenerSync.getMessage(messageID, DEFAULT_TIMEOUT, destination);
        // Assert
        assertEquals(message, receivedMessage);
        verify(connectionFactory, times(1)).createContext();
        verify(consumer, times(2)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldReconnectWhenErrorBroken() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT))
                .thenThrow(new JMSRuntimeException("error", "code", new Exception("Error CONNECTION_BROKEN")))
                .thenReturn(message);
        // Act
        Message receivedMessage = listenerSync.getMessage(messageID, DEFAULT_TIMEOUT, destination);
        // Assert
        assertEquals(message, receivedMessage);
        verify(connectionFactory, times(2)).createContext();
        verify(consumer, times(2)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldHandleErrorWhenRetryError() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT))
                .thenThrow(new JMSRuntimeException("error"))
                .thenThrow(new JMSRuntimeException("error"));
        // Act
        assertThrows(JMSRuntimeException.class, () -> listenerSync.getMessage(messageID, DEFAULT_TIMEOUT, destination));
        // Assert
        verify(consumer, times(2)).receive(DEFAULT_TIMEOUT);
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

    @Test
    void shouldNotDisconnect() throws JMSException {
        // Arrange
        MQContextMessageSelectorListenerSync listener = (MQContextMessageSelectorListenerSync) listenerSync.getRandom();
        // Act
        listener.disconnect();
        // Assert
        verify(consumer, never()).close();
    }

}

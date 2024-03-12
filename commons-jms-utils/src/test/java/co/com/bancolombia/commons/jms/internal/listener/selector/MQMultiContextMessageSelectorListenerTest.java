package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.api.exceptions.ReceiveTimeoutException;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.MultiContextSharedStrategy;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static co.com.bancolombia.commons.jms.internal.listener.selector.MQContextMessageSelectorListenerSync.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MQMultiContextMessageSelectorListenerTest {
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
    private MQQueuesContainer container;

    private MQMessageSelectorListener listener;

    @BeforeEach
    void setup() {
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        MQListenerConfig config = MQListenerConfig.builder()
                .concurrency(1)
                .connectionFactory(connectionFactory)
                .listeningQueue("QUEUE")
                .build();
        RetryableConfig retryableConfig = RetryableConfig
                .builder()
                .maxRetries(10)
                .initialRetryIntervalMillis(1000)
                .multiplier(1.5)
                .build();
        SelectorModeProvider provider = (cf, context) -> new MultiContextSharedStrategy(cf, 2);
        MQMessageSelectorListenerSync listenerSync =
                new MQMultiContextMessageSelectorListenerSync(config, healthListener,
                        retryableConfig, provider, container);
        listener = new MQMultiContextMessageSelectorListener(listenerSync);
    }

    @Test
    void shouldGetMessage() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Mono<Message> receiveMessage = listener.getMessage(messageID);
        // Assert
        StepVerifier.create(receiveMessage)
                .assertNext(receivedMessage -> assertEquals(message, receivedMessage))
                .verifyComplete();
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldGetMessageWithTimeout() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Mono<Message> receiveMessage = listener.getMessage(messageID, DEFAULT_TIMEOUT);
        // Assert
        StepVerifier.create(receiveMessage)
                .assertNext(receivedMessage -> assertEquals(message, receivedMessage))
                .verifyComplete();
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldHandleTimeoutErrorWithCustomTimeout() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(null);
        // Act
        Mono<Message> receiveMessage = listener.getMessage(messageID, DEFAULT_TIMEOUT, queue);
        // Assert
        StepVerifier.create(receiveMessage)
                .expectError(ReceiveTimeoutException.class)
                .verify();
    }

    @Test
    void shouldGetMessageBySelector() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Mono<Message> receiveMessage = listener.getMessageBySelector("JMSMessageID='" + messageID + "'");
        // Assert
        StepVerifier.create(receiveMessage)
                .assertNext(receivedMessage -> assertEquals(message, receivedMessage))
                .verifyComplete();
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldGetMessageBySelectorWithTimeout() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(message);
        // Act
        Mono<Message> receiveMessage = listener.getMessageBySelector("JMSMessageID='" + messageID + "'", DEFAULT_TIMEOUT);
        // Assert
        StepVerifier.create(receiveMessage)
                .assertNext(receivedMessage -> assertEquals(message, receivedMessage))
                .verifyComplete();
        verify(consumer, times(1)).receive(DEFAULT_TIMEOUT);
    }

    @Test
    void shouldHandleTimeoutErrorWithCustomTimeoutBySelector() {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createConsumer(any(Destination.class), anyString())).thenReturn(consumer);
        when(consumer.receive(DEFAULT_TIMEOUT)).thenReturn(null);
        // Act
        Mono<Message> receiveMessage = listener.getMessageBySelector("JMSMessageID='" + messageID + "'", DEFAULT_TIMEOUT, queue);
        // Assert
        StepVerifier.create(receiveMessage)
                .expectError(ReceiveTimeoutException.class)
                .verify();
    }

}

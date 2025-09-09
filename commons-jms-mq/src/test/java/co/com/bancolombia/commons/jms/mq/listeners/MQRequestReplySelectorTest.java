package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSelectorListener;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQRequestReplySelectorTest {
    @Mock
    private TextMessage message;
    @Mock
    private Queue destination;
    @Mock
    private Queue replyDestination;
    @Mock
    private JMSContext context;
    @Mock
    private MQMessageSender sender;
    @Mock
    private MQMessageSelectorListener listener;
    private MQRequestReplySelector reqReply;

    @BeforeEach
    public void setup() {
        String destinationQueue = "sample";
        String replyQueue = "sample";
        MQQueuesContainer container = new MQQueuesContainerImp();
        container.registerQueue(destinationQueue, destination);
        container.registerQueue(replyQueue, destination);
        reqReply = new MQRequestReplySelector(
                sender,
                container,
                destination,
                replyQueue,
                SelectorBuilder.ofDefaults(),
                listener
        );
    }

    @Test
    void shouldSendAndGetReplyFromFixed() {
        // Arrange
        when(sender.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(Mono.just("id"));
        when(listener.getMessageBySelector(anyString(), anyLong(), any(Destination.class))).thenReturn(Mono.just(message));
        // Act
        Mono<Message> reply = reqReply.requestReply("MyMessage");
        // Assert
        StepVerifier.create(reply)
                .assertNext(message1 -> assertEquals(message, message1))
                .verifyComplete();
    }

    @Test
    void shouldSendAndGetReplyFromFixedWithSpecificQueuesFromStringMessage() throws JMSException {
        // Arrange
        ArgumentCaptor<MQMessageCreator> creatorCaptor = ArgumentCaptor.forClass(MQMessageCreator.class);
        when(context.createTextMessage(anyString())).thenReturn(message);
        when(sender.send(any(Destination.class), creatorCaptor.capture())).thenReturn(Mono.just("id"));
        when(listener.getMessageBySelector(anyString(), anyLong(), any(Destination.class))).thenReturn(Mono.just(message));
        // Act
        Mono<Message> reply = reqReply.requestReply("MyMessage", destination, replyDestination, Duration.ofSeconds(1));
        // Assert
        StepVerifier.create(reply)
                .assertNext(message1 -> assertEquals(message, message1))
                .verifyComplete();
        MQMessageCreator capturedCreator = creatorCaptor.getValue();
        Message created = capturedCreator.create(context);
        assertEquals(message, created);
        verify(message).setJMSReplyTo(replyDestination);
    }

    @Test
    void shouldSendAndGetReplyFromFixedWithSpecificQueues() {
        // Arrange
        when(sender.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(Mono.just("id"));
        when(listener.getMessageBySelector(anyString(), anyLong(), any(Destination.class))).thenReturn(Mono.just(message));
        // Act
        Mono<Message> reply = reqReply.requestReply(ignored -> message, destination, replyDestination,
                Duration.ofSeconds(1));
        // Assert
        StepVerifier.create(reply)
                .assertNext(message1 -> assertEquals(message, message1))
                .verifyComplete();
    }

    @Test
    void shouldEnsureThatReplyToBeSet() throws JMSException {
        // Arrange
        ArgumentCaptor<MQMessageCreator> creatorCaptor = ArgumentCaptor.forClass(MQMessageCreator.class);
        when(sender.send(any(Destination.class), creatorCaptor.capture())).thenReturn(Mono.just("id"));
        when(listener.getMessageBySelector(anyString(), anyLong(), any(Destination.class))).thenReturn(Mono.just(message));
        // Act
        Mono<Message> reply = reqReply.requestReply(ignored -> message, destination, replyDestination,
                Duration.ofSeconds(1));
        // Assert
        StepVerifier.create(reply)
                .assertNext(message1 -> assertEquals(message, message1))
                .verifyComplete();

        MQMessageCreator capturedCreator = creatorCaptor.getValue();
        Message created = capturedCreator.create(context);
        assertEquals(message, created);
        verify(message).setJMSReplyTo(replyDestination);
    }

    @Test
    void shouldReplyWithTimeoutFromFixed() {
        // Arrange
        when(sender.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(Mono.just("id"));
        when(listener.getMessageBySelector(anyString(), anyLong(), any(Destination.class))).thenReturn(Mono.just(message));
        Duration duration = Duration.ofMillis(200);
        // Act
        Mono<Message> reply = reqReply.requestReply("MyMessage", duration);
        // Assert
        StepVerifier.create(reply)
                .assertNext(message1 -> assertEquals(message, message1))
                .verifyComplete();
    }

    @Test
    void shouldCreateDefaultMessageCreatorFromFixed() throws JMSException {
        // Arrange
        ArgumentCaptor<MQMessageCreator> creatorArgumentCaptor = ArgumentCaptor.forClass(MQMessageCreator.class);
        when(sender.send(eq(destination), creatorArgumentCaptor.capture())).thenReturn(Mono.just("id"));
        when(listener.getMessageBySelector(anyString(), anyLong(), any(Destination.class))).thenReturn(Mono.just(message));
        when(context.createTextMessage("MyMessage")).thenReturn(message);
        Duration duration = Duration.ofMillis(200);
        // Act
        Mono<Message> reply = reqReply.requestReply("MyMessage", duration);
        // Assert
        StepVerifier.create(reply).expectNext(message).verifyComplete();
        MQMessageCreator creator = creatorArgumentCaptor.getValue();
        Message createdMessage = creator.create(context);
        assertEquals(message, createdMessage);
    }

}

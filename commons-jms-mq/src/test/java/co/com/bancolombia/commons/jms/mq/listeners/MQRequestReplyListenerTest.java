package co.com.bancolombia.commons.jms.mq.listeners;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.InvalidUsageException;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQRequestReplyListenerTest {
    @Mock
    private TextMessage message;
    @Mock
    private Queue destination;
    @Mock
    private JMSContext context;
    @Mock
    private MQMessageSender sender;
    private MQRequestReplyListener listener;

    @BeforeEach
    void setup() {
        String queue = "sample";
        MQQueuesContainer container = new MQQueuesContainerImp();
        container.registerQueue(queue, destination);
        listener = new MQRequestReplyListener(sender, new ReactiveReplyRouter<>(), container, destination, queue,
                Message::getJMSCorrelationID, 1);
    }

    @Test
    void shouldSendAndGetReply() throws JMSException {
        // Arrange
        when(sender.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(Mono.just("id"));
        when(message.getJMSCorrelationID()).thenReturn("id");
        // Act
        Mono<Message> reply = listener.requestReply("MyMessage");
        reply.subscribe(message1 -> {
            // Assert
            assertEquals(message, message1);
            verify(sender, times(1)).send(any(Destination.class), any(MQMessageCreator.class));
        });
        listener.onMessage(message);
    }

    @Test
    void shouldHandleTimeoutReply() {
        // Arrange
        when(sender.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(Mono.just("id"));
        Duration duration = Duration.ofMillis(200);
        // Act
        Mono<Message> reply = listener.requestReply("MyMessage", duration);
        // Assert
        StepVerifier.create(reply).verifyTimeout(duration);
    }

    @Test
    void shouldCreateDefaultMessageCreator() throws JMSException {
        // Arrange
        ArgumentCaptor<MQMessageCreator> creatorArgumentCaptor = ArgumentCaptor.forClass(MQMessageCreator.class);
        when(sender.send(eq(destination), creatorArgumentCaptor.capture())).thenReturn(Mono.just("id"));
        when(context.createTextMessage("MyMessage")).thenReturn(message);
        Duration duration = Duration.ofMillis(200);
        // Act
        Mono<Message> reply = listener.requestReply("MyMessage", duration);
        // Assert
        StepVerifier.create(reply).verifyTimeout(duration);
        MQMessageCreator creator = creatorArgumentCaptor.getValue();
        Message createdMessage = creator.create(context);
        assertEquals(message, createdMessage);
    }

    @Test
    void shouldNotFailWhenNoRelatedMessage() throws JMSException {
        // Arrange
        when(message.getJMSCorrelationID()).thenReturn("non-existing-id");
        // Act
        listener.onMessage(message);
        // Assert
    }

    @Test
    void shouldFailWhenUsingFixedMethod() throws JMSException {
        // Arrange
        // Act
        Mono<Message> flow = listener.requestReply(context1 -> message, destination, destination,
                Duration.ofSeconds(1));
        // Assert
        StepVerifier.create(flow)
                .verifyError(InvalidUsageException.class);
    }

    @Test
    void shouldFailWhenUsingFixedMethodStringMessage() throws JMSException {
        // Arrange
        // Act
        Mono<Message> flow = listener.requestReply("message", destination, destination, Duration.ofSeconds(1));
        // Assert
        StepVerifier.create(flow)
                .verifyError(InvalidUsageException.class);
    }

}

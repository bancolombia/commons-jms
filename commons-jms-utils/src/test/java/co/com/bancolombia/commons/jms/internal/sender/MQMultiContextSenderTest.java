package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQMultiContextSenderTest {
    @Mock
    private MQMultiContextSenderSync senderSync;
    @Mock
    private Destination destination;
    private MQMultiContextSender sender;

    @BeforeEach
    void setup() {
        sender = new MQMultiContextSender(senderSync);
    }

    @Test
    void shouldSendString() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(anyString())).thenReturn(id);
        // Act
        Mono<String> monad = sender.send("message");
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

    @Test
    void shouldSend() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(any(MQMessageCreator.class))).thenReturn(id);
        // Act
        Mono<String> monad = sender.send(JMSContext::createTextMessage);
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

    @Test
    void shouldSendWithStringParams() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(anyString(), anyString())).thenReturn(id);
        // Act
        Mono<String> monad = sender.send("queue", "message");
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

    @Test
    void shouldSendWithStringAndMessageCreator() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(anyString(), any(MQMessageCreator.class))).thenReturn(id);
        // Act
        Mono<String> monad = sender.send("queue", JMSContext::createTextMessage);
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

    @Test
    void shouldSendWithDestinationAndString() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(any(Destination.class), anyString())).thenReturn(id);
        // Act
        Mono<String> monad = sender.send(destination, "message");
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

    @Test
    void shouldSendWithDestinationAndMessageCreator() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(id);
        // Act
        Mono<String> monad = sender.send(destination, JMSContext::createTextMessage);
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

}

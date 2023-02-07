package co.com.bancolombia.commons.jms.internal.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
    void shouldSend() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(any())).thenReturn(id);
        // Act
        Mono<String> monad = sender.send(JMSContext::createTextMessage);
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

    @Test
    void shouldSendWithDestination() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(senderSync.send(any(), any())).thenReturn(id);
        // Act
        Mono<String> monad = sender.send(destination, JMSContext::createTextMessage);
        // Assert
        StepVerifier.create(monad)
                .expectNext(id)
                .verifyComplete();
    }

}

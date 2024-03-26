package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import co.com.bancolombia.commons.jms.http.replier.api.LocationManager;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpReactiveReplyRouterTest {
    @Mock
    private LocationManager manager;

    @Mock
    private ReplyClient client;

    @Mock
    private TextMessage message;

    private HttpReactiveReplyRouter router;

    @BeforeEach
    void setup() {
        router = new HttpReactiveReplyRouter(client, manager, "http://localhost:5555/reply");
    }

    @Test
    void shouldReplyLocal() throws JMSException {
        // Arrange
        String mocked = "body";
        when(message.getText()).thenReturn(mocked);
        when(manager.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.empty());
        // Act
        Mono<JmsMessage> flow = router.wait("message-id");
        Mono<Void> replyFlow = router.remoteReply("message-id", message);
        // Assert
        StepVerifier.create(replyFlow).verifyComplete();
        StepVerifier.create(flow).expectNextMatches(response -> response.getBody().equals(mocked)).verifyComplete();
    }

    @Test
    void shouldReplyRemote() throws JMSException {
        // Arrange
        String mocked = "body";
        when(message.getText()).thenReturn(mocked);
        when(manager.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.empty());
        when(manager.get(anyString())).thenReturn(Mono.just("http://somehost:5555/reply"));
        when(client.remoteReply(anyString(), any(JmsMessage.class))).thenReturn(Mono.empty());
        // Act
        long timeout = 100L;
        Mono<JmsMessage> flow = router.wait("message-id", Duration.ofMillis(timeout));
        Mono<Void> replyFlow = router.remoteReply("message-id-2", message);
        // Assert
        StepVerifier.create(replyFlow).verifyComplete();
        StepVerifier.create(flow).expectError(TimeoutException.class).verify();
    }
}

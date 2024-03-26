package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class ReplyClientTest {

    private static MockWebServer mockBackEnd;
    private static ReplyClient client;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        int timeout = 1000;
        client = new ReplyClient(WebClient.builder(), timeout);
    }

    @Test
    void remoteReplyTest() {
        // Arrange
        mockBackEnd.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));
        JmsMessage message = JmsMessage.builder()
                .messageID("message-id")
                .correlationID("correlation-id")
                .timestamp(System.currentTimeMillis())
                .body("message body")
                .build();
        // Act
        Mono<Void> flow = client.remoteReply(mockBackEnd.url("/reply").toString(), message);
        // Assert
        StepVerifier.create(flow)
                .verifyComplete();
    }
}

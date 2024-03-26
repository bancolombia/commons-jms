package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.server.WebServer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyServerTest {

    @Mock
    private HttpReactiveReplyRouter router;

    @Test
    void startServer() {
        // Arrange
        // Act
        WebServer server = ReplyServer.startServer(router, 8080, false);
        // Assert
        assertNotNull(server);
    }

    @Test
    void httpRouter() {
        // Arrange
        // Act
        RouterFunction<ServerResponse> handler = ReplyServer.httpRouter(router);
        // Assert
        assertNotNull(handler);
    }

    @Test
    void replyHandler() {
        // Arrange
        ServerRequest request = mock(ServerRequest.class);
        JmsMessage jmsMessage = new JmsMessage();
        when(request.bodyToMono(JmsMessage.class)).thenReturn(Mono.just(jmsMessage));
        // Act
        Mono<ServerResponse> flow = ReplyServer.replyHandler(router, request);
        // Assert
        StepVerifier.create(flow)
                .expectNextCount(1)
                .verifyComplete();
        verify(router).reply(any(), any());
    }
}
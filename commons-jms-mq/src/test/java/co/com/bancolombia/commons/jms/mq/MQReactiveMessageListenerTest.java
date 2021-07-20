package co.com.bancolombia.commons.jms.mq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.jms.Message;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MQReactiveMessageListenerTest {
    @Mock
    private InvocableHandlerMethod handlerMethod;
    @Mock
    private Message message;
    @InjectMocks
    private MQReactiveMessageListener listener;

    @Test
    void shouldListen() {
        // Arrange
        when(handlerMethod.invoke(any(), any())).thenReturn(Mono.empty());
        // Act
        Mono<Object> listen = listener.onMessageAsync(message);

        StepVerifier.create(listen)
                .verifyComplete();
        // Assert
        verify(handlerMethod, times(1)).invoke(any(), any(Message.class));
    }

}

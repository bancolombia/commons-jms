package co.com.bancolombia.commons.jms.mq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;

import javax.jms.Message;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MQReactiveMessageListenerTest {
    @Mock
    private InvocableHandlerMethod handlerMethod;
    @Mock
    private Message message;
    @InjectMocks
    private MQReactiveMessageListener listener;

    @Test
    void shouldListen() throws InterruptedException {
        // Arrange
        // Act
        listener.onMessage(message);
        Thread.sleep(1000);
        // Assert
        verify(handlerMethod, times(1)).invoke(null, message);
    }

}

package co.com.bancolombia.commons.jms.mq.listeners;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.handler.invocation.reactive.InvocableHandlerMethod;
import reactor.core.publisher.Mono;

import static com.ibm.msg.client.jakarta.jms.JmsConstants.JMSX_DELIVERY_COUNT;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQReactiveMessageListenerTest {
    @Mock
    private InvocableHandlerMethod handlerMethod;
    @Mock
    private Message message;
    private MQReactiveMessageListener listener;

    @BeforeEach
    void setup() {
        listener = new MQReactiveMessageListener(handlerMethod, 1);
    }

    @Test
    void shouldListen() {
        // Arrange
        when(handlerMethod.invoke(any(), any())).thenReturn(Mono.empty());
        // Act
        listener.onMessage(message);
        // Assert
        verify(handlerMethod, times(1)).invoke(any(), any(Message.class));
    }

    @Test
    void shouldThrowError() throws JMSException {
        // Arrange
        when(handlerMethod.invoke(any(), any())).thenReturn(Mono.error(new RuntimeException()));
        when(message.getIntProperty(JMSX_DELIVERY_COUNT)).thenReturn(0);
        // Act
        // Assert
        assertThrows(RuntimeException.class, () -> listener.onMessage(message));
    }

    @Test
    void shouldHandleErrorWhenReachedRetriesAttempts() throws JMSException {
        // Arrange
        when(handlerMethod.invoke(any(), any())).thenReturn(Mono.error(new RuntimeException()));
        when(message.getIntProperty(JMSX_DELIVERY_COUNT)).thenReturn(2);
        // Act
        listener.onMessage(message);
        // Assert
        verify(handlerMethod, times(1)).invoke(any(), any(Message.class));
    }

}

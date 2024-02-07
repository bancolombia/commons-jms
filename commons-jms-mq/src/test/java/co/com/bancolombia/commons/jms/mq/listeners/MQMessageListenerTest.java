package co.com.bancolombia.commons.jms.mq.listeners;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQMessageListenerTest {
    @Mock
    private InvocableHandlerMethod handlerMethod;
    @Mock
    private Message message;
    private MQMessageListener listener;

    @BeforeEach
    void setup() {
        listener = new MQMessageListener(handlerMethod, 1);
    }

    @Test
    void shouldHandleMessage() throws Exception {
        // Arrange
        when(handlerMethod.invoke(any(), any(Message.class))).thenReturn("OK");
        // Act
        listener.onMessage(message);
        // Assert
        verify(handlerMethod, times(1)).invoke(any(), any(Message.class));
    }

    @Test
    void shouldRetry() throws Exception {
        // Arrange
        when(handlerMethod.invoke(any(), any(Message.class))).thenThrow(new JMSException("My Error"));
        // Act
        // Assert
        Assertions.assertThrows(JMSException.class, () -> listener.onMessage(message));
    }

    @Test
    void shouldDiscardRetry() throws Exception {
        // Arrange
        when(handlerMethod.invoke(any(), any(Message.class))).thenThrow(new JMSException("My Error"));
        when(message.getIntProperty(any())).thenReturn(2);
        // Act
        listener.onMessage(message);
        // Assert
        verify(message, atLeastOnce()).getJMSMessageID();
    }

}

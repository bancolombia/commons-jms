package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilsTest {

    @Mock
    private TextMessage textMessage;
    @Mock
    private BytesMessage bytesMessage;
    @Mock
    private ObjectMessage objectMessage;

    private final String expected = "message body";

    @Test
    void shouldGetStringMessage() throws JMSException {
        // Arrange
        when(textMessage.getText()).thenReturn(expected);
        when(textMessage.getJMSMessageID()).thenReturn("message-id");
        when(textMessage.getJMSCorrelationID()).thenReturn("correlation-id");
        when(textMessage.getJMSTimestamp()).thenReturn(1L);
        // Act
        JmsMessage result = Utils.fromMessage(textMessage);
        // Assert
        assertEquals(expected, result.getBody());
        assertEquals("message-id", result.getMessageID());
        assertEquals("correlation-id", result.getCorrelationID());
        assertEquals(1L, result.getTimestamp());
    }

    @Test
    void shouldGetBytesMessage() throws JMSException {
        // Arrange
        byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);
        when(bytesMessage.getBodyLength()).thenReturn((long) bytes.length);
        when(bytesMessage.readBytes(any())).thenAnswer(invocation -> {
            byte[] bytesCall = (byte[]) invocation.getArguments()[0];
            System.arraycopy(bytes, 0, bytesCall, 0, bytesCall.length);
            return bytesCall.length;
        });
        // Act
        JmsMessage result = Utils.fromMessage(bytesMessage);
        // Assert
        assertEquals(expected, result.getBody());
    }

    @Test
    void shouldGetObjectMessage() throws JMSException {
        // Arrange
        when(objectMessage.getBody(any())).thenReturn(expected);
        // Act
        JmsMessage result = Utils.fromMessage(objectMessage);
        // Assert
        assertEquals(expected, result.getBody());
    }

}

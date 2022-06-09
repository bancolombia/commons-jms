package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MQMultiContextSenderSyncTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private JMSContext context;
    @Mock
    private JMSProducer producer;
    @Mock
    private Queue queue;
    @Mock
    private TextMessage message;
    @Mock
    private MQProducerCustomizer customizer;
    @Mock
    private MQHealthListener healthListener;

    private MQMultiContextSenderSync senderSync;

    @BeforeEach
    void setup() {
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        when(context.createProducer()).thenReturn(producer);
        senderSync = new MQMultiContextSenderSync(connectionFactory, 2,
                ctx -> ctx.createQueue("QUEUE.NAME"), customizer, healthListener);
    }

    @Test
    void shouldSend() throws JMSException {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createTextMessage()).thenReturn(message);
        when(message.getJMSMessageID()).thenReturn(messageID);
        // Act
        String id = senderSync.send(JMSContext::createTextMessage);
        // Assert
        assertEquals(messageID, id);
        verify(producer, times(1)).send(queue, message);
    }

    @Test
    void shouldSendWithDestination() throws JMSException {
        // Arrange
        String messageID = UUID.randomUUID().toString();
        when(context.createTextMessage()).thenReturn(message);
        when(message.getJMSMessageID()).thenReturn(messageID);
        // Act
        String id = senderSync.send(queue, JMSContext::createTextMessage);
        // Assert
        assertEquals(messageID, id);
        verify(producer, times(1)).send(queue, message);
    }

    @Test
    void shouldHandleError() {
        // Arrange
        // Assert
        assertThrows(JMSRuntimeException.class, () -> {
            // Act
            senderSync.send(ctx -> {
                throw new JMSException("Error");
            });
        });
    }

}

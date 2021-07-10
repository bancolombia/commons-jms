package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MQQueueUtilsTest {
    @Mock
    private JMSContext context;
    @Mock
    private Session session;
    @Mock
    private MQQueueCustomizer customizer;
    @Mock
    private Queue queue;
    @Mock
    private TemporaryQueue temporaryQueue;
    private MQListenerConfig config;

    @BeforeEach
    void setup() {
        config = MQListenerConfig.builder()
                .queue("QUEUE.NAME")
                .customizer(customizer)
                .build();
    }

    @Test
    void shouldCreateFixedQueue() {
        // Arrange
        when(context.createQueue(anyString())).thenReturn(queue);
        // Act
        Destination destination = MQQueueUtils.setupFixedQueue(context, MQListenerConfig.builder()
                .queue("QUEUE.NAME")
                .customizer(null)
                .build());
        // Assert
        verify(context, times(1)).createQueue(anyString());
        assertEquals(destination, queue);
    }

    @Test
    void shouldCustomizeQueue() throws JMSException {
        // Arrange
        when(context.createQueue(anyString())).thenReturn(queue);
        // Act
        Destination destination = MQQueueUtils.setupFixedQueue(context, MQListenerConfig.builder()
                .queue("QUEUE.NAME")
                .customizer(Queue::getQueueName)
                .build());
        // Assert
        verify(context, times(1)).createQueue(anyString());
        verify(queue, times(1)).getQueueName();
        assertEquals(destination, queue);
    }

    @Test
    void shouldCreateTemporaryQueue() throws JMSException {
        // Arrange
        when(session.createTemporaryQueue()).thenReturn(temporaryQueue);
        // Act
        Destination destination = MQQueueUtils.setupTemporaryQueue(session, config);
        // Assert
        verify(session, times(1)).createTemporaryQueue();
        assertEquals(destination, temporaryQueue);
    }

    @Test
    void shouldCreateFailCreatingTemporaryQueue() throws JMSException {
        // Arrange
        when(session.createTemporaryQueue()).thenThrow(new JMSException("Any Error"));
        // Assert
        assertThrows(JMSRuntimeException.class, () -> {
            // Act
            MQQueueUtils.setupTemporaryQueue(session, config);
        });
    }

    @Test
    void shouldCreateFailCustomizingTemporaryQueue() throws JMSException {
        // Arrange
        when(session.createTemporaryQueue()).thenReturn(temporaryQueue);
        doThrow(new JMSException("Any Error")).when(customizer).customize(any());
        // Assert
        assertThrows(JMSRuntimeException.class, () -> {
            // Act
            MQQueueUtils.setupTemporaryQueue(session, config);
        });
    }
}

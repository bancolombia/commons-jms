package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Queue;
import jakarta.jms.TemporaryQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQQueueUtilsTest {
    @Mock
    private JMSContext context;
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
                .listeningQueue("QUEUE.NAME")
                .queueCustomizer(customizer)
                .build();
    }

    @Test
    void shouldCreateFixedQueue() {
        // Arrange
        when(context.createQueue(anyString())).thenReturn(queue);
        // Act
        Destination destination = MQQueueUtils.setupFixedQueue(context, MQListenerConfig.builder()
                .listeningQueue("QUEUE.NAME")
                .queueCustomizer(null)
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
                .listeningQueue("QUEUE.NAME")
                .queueCustomizer(Queue::getQueueName)
                .build());
        // Assert
        verify(context, times(1)).createQueue(anyString());
        verify(queue, times(1)).getQueueName();
        assertEquals(destination, queue);
    }

    @Test
    void shouldCreateTemporaryQueue() throws JMSException {
        // Arrange
        when(context.createTemporaryQueue()).thenReturn(temporaryQueue);
        // Act
        Destination destination = MQQueueUtils.setupTemporaryQueue(context, config);
        // Assert
        verify(context, times(1)).createTemporaryQueue();
        assertEquals(destination, temporaryQueue);
    }

    @Test
    void shouldCreateFailCreatingTemporaryQueue() throws JMSException {
        // Arrange
        when(context.createTemporaryQueue()).thenThrow(new JMSRuntimeException("Any Error"));
        // Assert
        assertThrows(JMSRuntimeException.class, () -> {
            // Act
            MQQueueUtils.setupTemporaryQueue(context, config);
        });
    }

    @Test
    void shouldNotHandleErrorWhenCustomizing() throws JMSException {
        // Arrange
        when(context.createTemporaryQueue()).thenReturn(temporaryQueue);
        doThrow(new JMSException("Any Error")).when(customizer).customize(any());
        // Act
        MQQueueUtils.setupTemporaryQueue(context, config);
        // Assert
        verify(customizer, times(1)).customize(any());

    }
}

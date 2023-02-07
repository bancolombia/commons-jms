package co.com.bancolombia.commons.jms.internal.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQConnectionListenerTest {
    @Mock
    private Session session;
    @Mock
    private TemporaryQueue destination;
    @Mock
    private MessageListener listener;
    @Mock
    private MessageConsumer consumer;

    private MQConnectionListener connectionListener;

    @BeforeEach
    void setup() throws JMSException {
        when(destination.getQueueName()).thenReturn("QM1/TEMPORARY.QUEUE?someArgument=value");
        connectionListener = MQConnectionListener.builder()
                .session(session)
                .destination(destination)
                .listener(listener)
                .sequence(1)
                .build();
    }

    @Test
    void shouldStartListener() throws JMSException {
        // Arrange
        when(session.createConsumer(destination)).thenReturn(consumer);
        // Act
        connectionListener.run();
        // Assert
        verify(consumer, times(1)).setMessageListener(listener);
    }

    @Test
    void shouldHandleError() throws JMSException {
        // Arrange
        when(session.createConsumer(destination)).thenThrow(new JMSException("Any Error"));
        // Assert
        assertThrows(JMSRuntimeException.class, () -> {
            // Act
            connectionListener.run();
        });
    }


}

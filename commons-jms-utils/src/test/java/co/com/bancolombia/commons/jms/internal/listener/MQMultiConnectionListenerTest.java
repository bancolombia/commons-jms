package co.com.bancolombia.commons.jms.internal.listener;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import jakarta.jms.TemporaryQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQMultiConnectionListenerTest {
    @Mock
    private Session session;
    @Mock
    private MessageListener listener;
    @Mock
    private Connection connection;

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private MQHealthListener healthListener;

    @Mock
    private TemporaryQueue tmpQueue;

    private MQMultiConnectionListener connectionListener;

    @BeforeEach
    void setup() throws JMSException {
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession()).thenReturn(session);
        when(session.createTemporaryQueue()).thenReturn(tmpQueue);
        connectionListener = MQMultiConnectionListener.builder()
                .listener(listener)
                .connection(connection)
                .connectionFactory(connectionFactory)
                .container(new MQQueuesContainerImp())
                .healthListener(healthListener)
                .config(MQListenerConfig.builder().build())
                .service(Executors.newCachedThreadPool())
                .build();
    }

    @Test
    void shouldStartListener() throws JMSException {
        // Arrange
        // Act
        connectionListener.call();
        // Assert
        verify(connection, times(1)).start();
    }

    @Test
    void shouldDisconnect() throws JMSException {
        // Arrange
        connectionListener.call();
        // Act
        connectionListener.disconnect();
        // Assert
        verify(connection, times(1)).close();
    }


}

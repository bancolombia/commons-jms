package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQAutoconfigurationSelectorListenerTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private JMSContext context;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private Queue queue;
    private final MQAutoconfigurationSelectorListener configurator = new MQAutoconfigurationSelectorListener();

    @Test
    void shouldMapConfiguration() {
        // Arrange
        int inputConcurrency = 1;
        String queueName = "QUEUE";
        MQProperties properties = new MQProperties();
        properties.setInputConcurrency(inputConcurrency);
        properties.setInputQueue(queueName);
        // Act
        MQListenerConfig config = configurator.defaultMQListenerConfig(properties, null);
        // Assert
        assertEquals(inputConcurrency, config.getConcurrency());
        assertEquals(queueName, config.getQueue());
    }

    @Test
    void shouldHandleError() {
        // Arrange
        MQListenerConfig config = MQListenerConfig.builder().concurrency(0).build();
        // Act
        // Assert
        assertThrows(MQInvalidListenerException.class,
                () -> configurator.defaultMQMultiContextMessageSelectorListenerSync(null, config, healthListener));
    }

    @Test
    void shouldCreateDefaultMessageSelectorListener() {
        // Arrange
        MQListenerConfig config = MQListenerConfig.builder()
                .concurrency(1)
                .queue("QUEUE")
                .build();
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createQueue(anyString())).thenReturn(queue);
        // Act
        MQMessageSelectorListenerSync listener = configurator.
                defaultMQMultiContextMessageSelectorListenerSync(connectionFactory, config, healthListener);
        // Assert
        assertNotNull(listener);
    }

}

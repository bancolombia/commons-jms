package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.helper.JmsContextImpl;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsReadablePropertyContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_RESOLVED_QUEUE_MANAGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQAutoconfigurationSelectorListenerTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private JMSContext context;
    @Mock
    private JmsReadablePropertyContext propertyContext;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private MQQueue queue;
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
        MQListenerConfig config = configurator.messageSelectorListenerConfig(properties, null, null);
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

    @Test
    void shouldMapConfigurationWithQueueManager() throws JMSException {
        // Arrange
        when(propertyContext.getStringProperty(WMQ_RESOLVED_QUEUE_MANAGER)).thenReturn("QM1");
        doReturn(null).when(queue).getBaseQueueManagerName();
        int inputConcurrency = 1;
        String queueName = "QUEUE";
        MQProperties properties = new MQProperties();
        properties.setInputConcurrency(inputConcurrency);
        properties.setInputQueue(queueName);
        properties.setInputQueueSetQueueManager(true);
        MQQueuesContainer container = new MQQueuesContainerImp();
        MQQueueManagerSetter setter = configurator.qmSetter(properties, container);
        // Act
        MQListenerConfig config = configurator.messageSelectorListenerConfig(properties, null, setter);
        // Assert
        assertEquals(inputConcurrency, config.getConcurrency());
        assertEquals(queueName, config.getQueue());
        config.getQmSetter().accept(new JmsContextImpl(propertyContext), queue);
        verify(queue, atLeastOnce()).setBaseQueueManagerName("QM1");
        assertEquals(container.get(queueName), queue);
    }

}

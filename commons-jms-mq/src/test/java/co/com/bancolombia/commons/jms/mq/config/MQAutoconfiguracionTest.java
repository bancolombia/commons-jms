package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import com.ibm.mq.jakarta.jms.MQQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_TARGET_CLIENT;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MQAutoconfiguracionTest {
    private final MQAutoconfiguration configuration = new MQAutoconfiguration();
    @Mock
    private MQQueue queue;
    @Mock
    private JMSContext context;

    @Test
    void shouldCreateCustomizer() throws JMSException {
        MQQueueCustomizer customizer = configuration.defaultMQQueueCustomizer();
        customizer.customize(queue);
        verify(queue, times(1)).setProperty(WMQ_TARGET_CLIENT, "1");
    }

    @Test
    void shouldCreateContainers() {
        MQQueuesContainer container = configuration.defaultMQQueuesContainer();
        Assertions.assertNull(container.get("non-existent"));
    }

    @Test
    void shouldCreateBrokerUtils() throws JMSException {
        MQBrokerUtils utils = configuration.defaultMqBrokerUtils();
        utils.setQueueManager(context, queue);
        verify(queue, times(1)).setBaseQueueManagerName("");
    }
}

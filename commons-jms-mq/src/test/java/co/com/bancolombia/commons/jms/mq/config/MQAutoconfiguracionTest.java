package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQExecutorService;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.ApplicationEventPublisher;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_TARGET_CLIENT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQAutoconfiguracionTest {
    private final MQAutoconfiguration configuration = new MQAutoconfiguration();
    @Mock
    private MQQueue queue;
    @Mock
    private JMSContext context;
    @Mock
    private JMSProducer producer;
    @Mock
    private ApplicationEventPublisher publisher;

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

    @Test
    void shouldCreateMQDestinationProvider() {
        when(context.createQueue(anyString())).thenReturn(queue);
        MQDestinationProvider provider = configuration.defaultMqDestinationProvider(
                configuration.defaultMQQueueCustomizer(), MQProperties.builder().outputQueue("sample").build());
        provider.create(context);
        verify(context, times(1)).createQueue("sample");
    }

    @Test
    void shouldCreateMQProducerCustomizer() {
        MQProducerCustomizer customizer = configuration.defaultMQProducerCustomizer(MQProperties.builder()
                .producerTtl(5000)
                .build());
        customizer.customize(producer);
        verify(producer, times(1)).setTimeToLive(5000);
    }

    @Test
    void shouldCreateMQQueueManagerSetter() throws JMSException {
        MQQueueManagerSetter setter = configuration.qmSetter(MQProperties.builder()
                        .inputQueueSetQueueManager(true)
                        .inputQueue("sample")
                        .build(),
                configuration.defaultMQQueuesContainer());
        setter.accept(context, queue);
        verify(queue, times(1)).setBaseQueueManagerName("");
    }

    @Test
    void shouldCreateMQListenerHealthIndicator() {
        MQHealthListener bean = configuration.defaultMqHealthListener(publisher, true);
        Assertions.assertNotNull(bean);
        assertInstanceOf(HealthIndicator.class, bean);
    }

    @Test
    void shouldCreateDisabledMQListenerHealthIndicator() {
        MQHealthListener bean = configuration.defaultMqHealthListener(publisher, false);
        bean.onInit("listener");
        bean.onStarted("listener");
        bean.onException("listener", new JMSException("test"));
        Assertions.assertNotNull(bean);
        assertFalse(bean instanceof HealthIndicator);
    }

    @Test
    void shouldCreateRetryableConfig() {
        RetryableConfig bean = configuration.defaultRetryableConfig(MQProperties.builder().build());
        Assertions.assertNotNull(bean);
    }

    @Test
    void shouldCreateSelectorBuilder() {
        SelectorBuilder bean = configuration.defaultSelectorBuilder();
        Assertions.assertNotNull(bean);
    }

    @Test
    void shouldCreateMQExecutorService() {
        MQExecutorService bean = configuration.defaultMqExecutorService();
        Assertions.assertNotNull(bean);
    }

    @Test
    void shouldCreateReactiveReplyRouter() {
        ReactiveReplyRouter<Message> bean = configuration.selectorReactiveReplyRouter();
        Assertions.assertNotNull(bean);
    }
}

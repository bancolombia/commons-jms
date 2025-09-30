package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.api.model.MQClient;
import co.com.bancolombia.commons.jms.api.model.MQMessageHandler;
import co.com.bancolombia.commons.jms.api.model.spec.CommonsJMSSpec;
import co.com.bancolombia.commons.jms.api.model.spec.MQDomainSpec;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQSchedulerProvider;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.TemporaryQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.scheduler.Schedulers;

import static co.com.bancolombia.commons.jms.mq.config.MQAutoconfiguration.KEEP_ALIVE_SECONDS;
import static co.com.bancolombia.commons.jms.mq.config.MQAutoconfiguration.MAX_THREADS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MQFromSpecConfigurationTest {
    @Mock
    private ConnectionFactory factory1;
    @Mock
    private MQMessageHandler handler;
    @Mock
    private MQSpringResolver resolver;
    @Mock
    private MQQueueCustomizer customizer;
    @Mock
    private MQProducerCustomizer producerCustomizer;
    @Mock
    private MQBrokerUtils brokerUtils;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private MQQueuesContainer container;
    @Mock
    private JMSContext context;
    @Mock
    private JMSConsumer consumer;
    @Mock
    private JMSProducer producer;
    @Mock
    private TemporaryQueue queue;

    @Test
    void shouldCreateMQClient() {
        // Arrange
        MQFromSpecConfiguration config = new MQFromSpecConfiguration();
        // Act
        MQClient client = config.mqClient();
        // Assert
        assertNotNull(client);
    }

    @Test
    void shouldCreateListenersFromSpec() {
        // Arrange
        MQFromSpecConfiguration config = new MQFromSpecConfiguration();

        MQClient client = config.mqClient();

        doReturn(new MQProperties()).when(resolver).getProperties();
        doReturn(customizer).when(resolver).resolveBean(MQQueueCustomizer.class);
        doReturn(client).when(resolver).resolveBean(MQClient.class);
        doReturn(container).when(resolver).getQueuesContainer();
        doReturn(healthListener).when(resolver).getHealthListener();
        doReturn(brokerUtils).when(resolver).getBrokerUtils();
        doReturn(RetryableConfig.builder().build()).when(resolver).getRetryableConfig();

        CommonsJMSSpec spec = CommonsJMSSpec.builder()
                .withDomain(MQDomainSpec.builder("domainA", factory1)
                        .listenQueue("DEV.QUEUE.1", handler)
                        .build())
                .build();
        // Act
        boolean result = config.mqClientFromSpec(spec, resolver);
        // Assert
        assertTrue(result);
    }

    @Test
    void shouldCreateSenderFromSpec() {
        // Arrange
        MQFromSpecConfiguration config = new MQFromSpecConfiguration();

        MQClient client = config.mqClient();

        doReturn(new MQProperties()).when(resolver).getProperties();
        doReturn(customizer).when(resolver).resolveBean(MQQueueCustomizer.class);
        doReturn(producerCustomizer).when(resolver).resolveBean(MQProducerCustomizer.class);
        doReturn(client).when(resolver).resolveBean(MQClient.class);
        doReturn(healthListener).when(resolver).getHealthListener();
        doReturn(context).when(factory1).createContext();
        doReturn(producer).when(context).createProducer();

        CommonsJMSSpec spec = CommonsJMSSpec.builder()
                .withDomain(MQDomainSpec.builder("domainA", factory1)
                        .withSender()
                        .build())
                .build();
        // Act
        boolean result = config.mqClientFromSpec(spec, resolver);
        // Assert
        assertTrue(result);
        verify(context).createProducer();
    }

    @Test
    void shouldCreateFixedReqReplyFromSpec() {
        // Arrange
        MQFromSpecConfiguration config = new MQFromSpecConfiguration();

        MQClient client = config.mqClient();

        doReturn(new MQProperties()).when(resolver).getProperties();
        doReturn(customizer).when(resolver).resolveBean(MQQueueCustomizer.class);
        doReturn(producerCustomizer).when(resolver).resolveBean(MQProducerCustomizer.class);
        doReturn(client).when(resolver).resolveBean(MQClient.class);
        doReturn(container).when(resolver).getQueuesContainer();
        doReturn(healthListener).when(resolver).getHealthListener();
        MQSchedulerProvider provider = () -> Schedulers.newBoundedElastic(MAX_THREADS, 2, "selector-pool",
                KEEP_ALIVE_SECONDS);
        doReturn(provider).when(resolver).getMqExecutorService();
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(resolver).resolveString(anyString());
        doReturn(brokerUtils).when(resolver).getBrokerUtils();
        doReturn(RetryableConfig.builder().build()).when(resolver).getRetryableConfig();
        doReturn(context).when(factory1).createContext();
        doReturn(producer).when(context).createProducer();

        CommonsJMSSpec spec = CommonsJMSSpec.builder()
                .withDomain(MQDomainSpec.builder("domainA", factory1)
                        .withFixedRequestReply()
                        .build())
                .build();
        // Act
        boolean result = config.mqClientFromSpec(spec, resolver);
        // Assert
        assertTrue(result);
        verify(context).createProducer();
        verify(factory1, times(2)).createContext();
    }

    @Test
    void shouldCreateTemporaryReqReplyFromSpec() {
        // Arrange
        MQFromSpecConfiguration config = new MQFromSpecConfiguration();

        MQClient client = config.mqClient();

        doReturn(new MQProperties()).when(resolver).getProperties();
        doReturn(customizer).when(resolver).resolveBean(MQQueueCustomizer.class);
        doReturn(producerCustomizer).when(resolver).resolveBean(MQProducerCustomizer.class);
        doReturn(client).when(resolver).resolveBean(MQClient.class);
        doReturn(container).when(resolver).getQueuesContainer();
        doReturn(healthListener).when(resolver).getHealthListener();
        doReturn(brokerUtils).when(resolver).getBrokerUtils();
        doReturn(RetryableConfig.builder().build()).when(resolver).getRetryableConfig();
        doReturn(context).when(factory1).createContext();
        doReturn(producer).when(context).createProducer();

        CommonsJMSSpec spec = CommonsJMSSpec.builder()
                .withDomain(MQDomainSpec.builder("domainA", factory1)
                        .withTemporaryRequestReply()
                        .build())
                .build();
        // Act
        boolean result = config.mqClientFromSpec(spec, resolver);
        // Assert
        assertTrue(result);
        verify(context).createProducer();
    }
}

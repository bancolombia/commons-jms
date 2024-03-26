package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.config.senders.MQSenderContainer;
import co.com.bancolombia.commons.jms.mq.listeners.MQExecutorService;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.util.StringValueResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQSpringResolverTest {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private RetryableConfig retryableConfig;
    @Mock
    private MQExecutorService executor;
    @Mock
    private MQBrokerUtils brokerUtils;
    @Mock
    private ObjectProvider<Object> provider;
    @Mock
    private MQSenderContainer senderContainer;
    @Mock
    private SelectorBuilder selectorBuilder;
    @Mock
    private BeanFactory beanFactory;
    @Mock
    private StringValueResolver stringValueResolver;

    private MQSpringResolver resolver;

    @BeforeEach
    public void setup() {
        resolver = new MQSpringResolver(beanFactory);
        resolver.setEmbeddedValueResolver(stringValueResolver);
    }


    @Test
    void shouldInstanceTheBean() {
        // Arrange
        when(provider.getIfAvailable(any())).thenReturn(new ReactiveReplyRouter<Message>());
        when(beanFactory.getBeanProvider(any(ResolvableType.class))).thenReturn(provider);
        when(stringValueResolver.resolveStringValue(anyString()))
                .thenAnswer((Answer<String>) invocation -> (String) invocation.getArguments()[0]);
        when(beanFactory.getBean(any(Class.class)))
                .thenAnswer(invocation -> {
                    Object arguments = invocation.getArguments()[0];
                    if (arguments.equals(MQQueuesContainer.class)) {
                        return new MQQueuesContainerImp();
                    }
                    if (arguments.equals(MQProperties.class)) {
                        return new MQProperties();
                    }
                    if (arguments.equals(MQBrokerUtils.class)) {
                        return brokerUtils;
                    }
                    if (arguments.equals(MQHealthListener.class)) {
                        return healthListener;
                    }
                    if (arguments.equals(RetryableConfig.class)) {
                        return retryableConfig;
                    }
                    if (arguments.equals(MQExecutorService.class)) {
                        return executor;
                    }
                    if (arguments.equals(MQQueueManagerSetter.class)) {
                        return (MQQueueManagerSetter) (tx, queue) -> {
                        };
                    }
                    if (arguments.equals(MQQueueCustomizer.class)) {
                        return (MQQueueCustomizer) queue -> {
                        };
                    }
                    if (arguments.equals(MQSenderContainer.class)) {
                        return senderContainer;
                    }
                    if (arguments.equals(SelectorBuilder.class)) {
                        return selectorBuilder;
                    }
                    if (arguments.equals(ConnectionFactory.class)) {
                        return connectionFactory;
                    }
                    return null;
                });
        // Act
        // Assert
        assertNotNull(resolver.getResolver());
        assertNotNull(resolver.getConnectionFactory(null));
        assertNotNull(resolver.getQueuesContainer());
        assertNotNull(resolver.getProperties());
        assertNotNull(resolver.getBrokerUtils());
        assertNotNull(resolver.getHealthListener());
        assertNotNull(resolver.getRetryableConfig());
        assertNotNull(resolver.getMqExecutorService());
        assertNotNull(resolver.getMqQueueManagerSetter());
        assertNotNull(resolver.getMqSenderContainer());
        assertNotNull(resolver.getSelectorBuilder());
        assertNotNull(resolver.resolveReplier());
        assertEquals("JUAN", resolver.resolveString("JUAN"));
    }
}

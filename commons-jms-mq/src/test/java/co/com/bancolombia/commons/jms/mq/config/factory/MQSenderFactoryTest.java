package co.com.bancolombia.commons.jms.mq.config.factory;

import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.api.model.spec.MQDomainSpec;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.internal.sender.MQMultiContextSender;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQSenderFactoryTest {

    private MQSpringResolver createResolverMock() {
        MQSpringResolver resolver = mock(MQSpringResolver.class);

        MQProperties properties = new MQProperties();
        properties.setOutputConcurrency(2);
        properties.setOutputQueue("OUTPUT.QUEUE");

        lenient().when(resolver.getProperties()).thenReturn(properties);
        lenient().when(resolver.getHealthListener()).thenReturn(mock(MQHealthListener.class));
        lenient().when(resolver.getRetryableConfig()).thenReturn(RetryableConfig.builder().build());
        lenient().doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
                .when(resolver).resolveString(anyString());

        return resolver;
    }

    private ConnectionFactory createConnectionFactoryMock() {
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        JMSContext jmsContext = mock(JMSContext.class);
        JMSProducer jmsProducer = mock(JMSProducer.class);
        when(connectionFactory.createContext()).thenReturn(jmsContext);
        when(jmsContext.createProducer()).thenReturn(jmsProducer);
        return connectionFactory;
    }

    @Test
    void shouldUseBothCustomizersWhenBothPresent() {
        // Arrange
        MQSpringResolver resolver = createResolverMock();
        ConnectionFactory connectionFactory = createConnectionFactoryMock();

        MQProducerCustomizer resolverProducerCustomizer = mock(MQProducerCustomizer.class);
        MQProducerCustomizer specProducerCustomizer = mock(MQProducerCustomizer.class);
        MQQueueCustomizer resolverQueueCustomizer = mock(MQQueueCustomizer.class);
        MQQueueCustomizer specQueueCustomizer = mock(MQQueueCustomizer.class);

        when(resolver.resolveBean(MQProducerCustomizer.class)).thenReturn(resolverProducerCustomizer);
        when(resolver.resolveBean(MQQueueCustomizer.class)).thenReturn(resolverQueueCustomizer);
        when(resolverProducerCustomizer.andThen(specProducerCustomizer))
                .thenReturn(mock(MQProducerCustomizer.class));
        when(resolverQueueCustomizer.andThen(specQueueCustomizer))
                .thenReturn(mock(MQQueueCustomizer.class));

        MQDomainSpec spec = MQDomainSpec.builder("testDomain", connectionFactory)
                .withProducerCustomizer(specProducerCustomizer)
                .withQueueCustomizer(specQueueCustomizer)
                .build();

        // Act
        MQMultiContextSender result = MQSenderFactory.forConnectionFactory(spec, resolver);

        // Assert
        assertNotNull(result);
        verify(resolverProducerCustomizer).andThen(specProducerCustomizer);
        verify(resolverQueueCustomizer).andThen(specQueueCustomizer);
    }

    @Test
    void shouldUseSpecProducerCustomizerWhenResolverReturnsNull() {
        // Arrange
        MQSpringResolver resolver = createResolverMock();
        ConnectionFactory connectionFactory = createConnectionFactoryMock();

        MQProducerCustomizer specProducerCustomizer = mock(MQProducerCustomizer.class);
        MQQueueCustomizer resolverQueueCustomizer = mock(MQQueueCustomizer.class);

        when(resolver.resolveBean(MQProducerCustomizer.class)).thenReturn(null);
        when(resolver.resolveBean(MQQueueCustomizer.class)).thenReturn(resolverQueueCustomizer);

        MQDomainSpec spec = MQDomainSpec.builder("testDomain", connectionFactory)
                .withProducerCustomizer(specProducerCustomizer)
                .build();

        // Act
        MQMultiContextSender result = MQSenderFactory.forConnectionFactory(spec, resolver);

        // Assert
        assertNotNull(result);
    }

    @Test
    void shouldHandleNullProducerCustomizersGracefully() {
        // Arrange
        MQSpringResolver resolver = createResolverMock();
        ConnectionFactory connectionFactory = createConnectionFactoryMock();

        when(resolver.resolveBean(MQProducerCustomizer.class)).thenReturn(null);
        when(resolver.resolveBean(MQQueueCustomizer.class)).thenReturn(null);

        MQDomainSpec spec = MQDomainSpec.builder("testDomain", connectionFactory).build();

        // Act
        MQMultiContextSender result = MQSenderFactory.forConnectionFactory(spec, resolver);

        // Assert
        assertNotNull(result);
    }

    @Test
    void shouldCreateValidSenderWithSpecCustomizersOnly() {
        // Arrange
        MQSpringResolver resolver = createResolverMock();
        ConnectionFactory connectionFactory = createConnectionFactoryMock();

        MQProducerCustomizer specProducerCustomizer = p -> p.setTimeToLive(5000L);
        MQQueueCustomizer specQueueCustomizer = q -> q.getQueueName();

        when(resolver.resolveBean(MQProducerCustomizer.class)).thenReturn(null);
        when(resolver.resolveBean(MQQueueCustomizer.class)).thenReturn(null);

        MQDomainSpec spec = MQDomainSpec.builder("testDomain", connectionFactory)
                .withProducerCustomizer(specProducerCustomizer)
                .withQueueCustomizer(specQueueCustomizer)
                .build();

        // Act
        MQMultiContextSender result = MQSenderFactory.forConnectionFactory(spec, resolver);

        // Assert
        assertNotNull(result);
    }
}


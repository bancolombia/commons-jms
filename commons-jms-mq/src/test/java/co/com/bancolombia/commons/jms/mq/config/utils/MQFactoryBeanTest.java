package co.com.bancolombia.commons.jms.mq.config.utils;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.senders.MQSenderContainer;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQExecutorService;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.TemporaryQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringValueResolver;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQFactoryBeanTest {
    private final RetryableConfig retryableConfig = RetryableConfig.builder().build();
    private final MQSenderContainer senderContainer = new MQSenderContainer();
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private MQExecutorService executor;
    @Mock
    private MQBrokerUtils brokerUtils;
    @Mock
    private ObjectProvider<Object> provider;
    @Mock
    private MQMessageSenderSync senderSync;
    @Mock
    private MQMessageSender sender;
    @Mock
    private SelectorBuilder selectorBuilder;
    @Mock
    private StringValueResolver stringValueResolver;
    @Mock
    private MQSpringResolver resolver;
    @Mock
    private JMSContext context;
    @Mock
    private JMSProducer producer;
    @Mock
    private JMSConsumer consumer;
    @Mock
    private TemporaryQueue tempQueue;
    private MQFactoryBean<? extends Annotation> scanner;

    void setup(AnnotationMetadata metadata, Class<? extends Annotation> annotation) {
        when(resolver.resolveString(anyString()))
                .thenAnswer((Answer<String>) invocation -> (String) invocation.getArguments()[0]);
        when(resolver.getBrokerUtils()).thenReturn(brokerUtils);
        when(resolver.getRetryableConfig()).thenReturn(retryableConfig);
        when(resolver.getHealthListener()).thenReturn(healthListener);
        when(resolver.getMqSenderContainer()).thenReturn(senderContainer);
        when(resolver.resolveBean("", MQQueueCustomizer.class)).thenReturn(queue -> {
        });
        when(resolver.resolveBean("", MQProducerCustomizer.class)).thenReturn(producer -> {
        });
        when(resolver.resolveBean("", RetryableConfig.class)).thenReturn(retryableConfig);
        when(resolver.getConnectionFactory(anyString())).thenReturn(connectionFactory);
        when(resolver.getQueuesContainer()).thenReturn(new MQQueuesContainerImp());
        scanner = new MQFactoryBean<>(metadata, annotation, resolver);

//        when(provider.getIfAvailable(any())).thenReturn(new ReactiveReplyRouter<Message>());

//        when(resolver.getProvider(any())).thenReturn(provider);
//        when(resolver.getResolver()).thenReturn(stringValueResolver);
//        when(resolver.getMqExecutorService()).thenReturn(executor);
//        when(resolver.getSelectorBuilder()).thenReturn(selectorBuilder);
//        when(resolver.getMqQueueManagerSetter()).thenReturn((tx, queue) -> {
//        });
    }

    @Test
    void shouldCreateTempReqReplyInstance() throws JMSException {
        // Arrange
        setup(Utils.getMetadataReqReply(), ReqReply.class);
        when(resolver.getProperties()).thenReturn(MQProperties.builder().reactive(true).build());
        when(resolver.resolveBean(MQMessageSender.class)).thenReturn(sender);
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createProducer()).thenReturn(producer);
        when(context.createConsumer(any())).thenReturn(consumer);
        when(context.createTemporaryQueue()).thenReturn(tempQueue);
        when(tempQueue.getQueueName()).thenReturn("MQ-TEMP-QUEUE");
        // Act
        Object generated = scanner.getObject();
        // Assert
        assertTrue(generated instanceof MQRequestReply);
    }

    @Test
    void shouldCreateFixedReqReplyInstance() {
        // Arrange
        setup(Utils.getMetadataReqReplyFixed(), ReqReply.class);
        when(resolver.getProperties()).thenReturn(MQProperties.builder().reactive(true).build());
        when(resolver.resolveBean(MQMessageSender.class)).thenReturn(sender);
        when(connectionFactory.createContext()).thenReturn(context);
        when(context.createProducer()).thenReturn(producer);
        when(context.createQueue(anyString())).thenReturn(tempQueue);
        // Act
        Object generated = scanner.getObject();
        // Assert
        assertTrue(generated instanceof MQRequestReply);
    }
}

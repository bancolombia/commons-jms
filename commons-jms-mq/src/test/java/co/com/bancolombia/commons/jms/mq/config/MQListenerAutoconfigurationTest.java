package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MQListenerAutoconfigurationTest {
    @Mock
    private ConfigurableBeanFactory factory;
    @Mock
    private MQQueuesContainer container;
    @Mock
    private ConnectionFactory cf;
    @Mock
    private ConnectionFactory cf2;
    @Mock
    private MQQueueCustomizer customizer;
    @Mock
    private MQBrokerUtils brokerUtils;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private RetryableConfig retryableConfig;
    @Mock
    private MQSpringResolver resolver;
    @InjectMocks
    private MQListenerAutoconfiguration processor;

    @BeforeEach
    void setup() {
        doReturn(cf).when(resolver).getConnectionFactory("");
        doAnswer(invocation -> invocation.getArguments()[0]).when(resolver).resolveString(anyString());
    }

    void commonMocks() {
        doReturn(container).when(resolver).getQueuesContainer();
        doReturn(healthListener).when(resolver).getHealthListener();
        doReturn(brokerUtils).when(resolver).getBrokerUtils();
    }

    @Test
    void shouldProcessAnnotated() {
        // Arrange
        commonMocks();
        doReturn(new MQProperties()).when(resolver).getProperties();
        doReturn(cf2).when(resolver).getConnectionFactory("custom");
        Object bean = new MyListener();
        // Act
        Object result = processor.postProcessAfterInitialization(bean, "MyListener");
        // Assert
        assertEquals(bean, result);
    }

    @Test
    void shouldProcessAnnotatedReactive() {
        // Arrange
        commonMocks();
        Object bean = new MyReactiveListener();
        MQProperties properties = new MQProperties();
        properties.setReactive(true);
        doReturn(new MQProperties()).when(resolver).getProperties();
        doReturn(cf2).when(resolver).getConnectionFactory("custom");
        // Act
        Object result = processor.postProcessAfterInitialization(bean, "MyReactiveListener");
        // Assert
        assertEquals(bean, result);
    }

    @Test
    void shouldWorksWithInvalidConcurrency() {
        // Arrange
        commonMocks();
        MQProperties properties = new MQProperties();
        properties.setInputConcurrency(0);
        properties.setReactive(true);
        doReturn(new MQProperties()).when(resolver).getProperties();
        Object bean = new MyReactiveListenerInvalidConcurrency();
        // Act
        Object result = processor.postProcessAfterInitialization(bean, "MyReactiveListenerInvalidConcurrency");
        // Assert
        assertEquals(bean, result);
    }

    @Test
    void shouldFailWithInvalidNoQueues() {
        // Arrange
        MQProperties properties = new MQProperties();
        properties.setReactive(true);
        doReturn(new MQProperties()).when(resolver).getProperties();
        Object bean = new MyReactiveListenerInvalidNoQueues();
        // Assert
        assertThrows(MQInvalidListenerException.class, () -> {
            // Act
            processor.postProcessAfterInitialization(bean, "MyReactiveListenerInvalidNoQueues");
        });
    }

    @Log
    public static class MyListener {

        @MQListener(value = "QUEUE.NAME", connectionFactory = "custom")
        public void process(Message message) throws JMSException {
            log.info("message: " + message.getJMSMessageID());
        }

        @MQListener("QUEUE.NAME2")
        public void process2(Message message) throws JMSException {
            log.info("message: " + message.getJMSMessageID());
        }
    }

    public static class MyReactiveListener {

        @MQListener(value = "QUEUE.NAME", connectionFactory = "custom")
        public Mono<Void> process(Message message) {
            return Mono.empty();
        }

        @MQListener("QUEUE.NAME2")
        public Mono<Void> process2(Message message) {
            return Mono.empty();
        }
    }

    public static class MyReactiveListenerInvalidConcurrency {

        @MQListener(value = "QUEUE.NAME", concurrency = "-1")
        public Mono<Void> process(Message message) {
            return Mono.empty();
        }
    }

    public static class MyReactiveListenerInvalidNoQueues {

        @MQListener
        public Mono<Void> process(Message message) {
            return Mono.empty();
        }
    }
}

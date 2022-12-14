package co.com.bancolombia.commons.jms.mq.config.proxy;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotationMetadata;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterfaceComponentProxyFactoryBeanTest {

    @Mock
    private ConfigurableBeanFactory beanFactory;
    @Mock
    private MQMessageSender sender;
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private Connection connection;
    @Mock
    private Session session;
    @Mock
    private TemporaryQueue queue;
    @Mock
    private ObjectProvider<Object> provider;
    //    @Mock
//    private MQMessageSender sender;
    private InterfaceComponentProxyFactoryBean factoryBean;

    @BeforeEach
    void setup() {
        AnnotationMetadata metadata = Utils.getMetadataReqReply();
        factoryBean = new InterfaceComponentProxyFactoryBean(metadata);
        factoryBean.setBeanFactory(beanFactory);
    }

    @Test
    void shouldReturnObjectType() {
        // Arrange
        // Act
        Class<?> clazz = factoryBean.getObjectType();
        // Assert
        assertNotNull(clazz);
        assertEquals("TestCustomAnnotation", clazz.getSimpleName());
    }

    @Test
    void shouldInstanceTheBean() throws JMSException {
        // Arrange
        when(provider.getIfAvailable(any())).thenReturn(new ReactiveReplyRouter<Message>());
        when(beanFactory.getBeanProvider(any(ResolvableType.class))).thenReturn(provider);
        when(beanFactory.resolveEmbeddedValue(anyString()))
                .thenAnswer((Answer<String>) invocation -> (String) invocation.getArguments()[0]);
        when(beanFactory.getBean(any(Class.class)))
                .thenAnswer(invocation -> {
                    Object arguments = invocation.getArguments()[0];
                    if (arguments.equals(MQProperties.class)) {
                        return new MQProperties();
                    }
                    if (arguments.equals(MQBrokerUtils.class)) {
                        return (MQBrokerUtils) (context, queue) -> {
                        };
                    }
                    if (arguments.equals(MQQueueCustomizer.class)) {
                        return (MQQueueCustomizer) queue -> {
                        };
                    }
                    if (arguments.equals(MQQueuesContainer.class)) {
                        return new MQQueuesContainerImp();
                    }
                    if (arguments.equals(MQMessageSender.class)) {
                        return sender;
                    }
                    if (arguments.equals(ConnectionFactory.class)) {
                        return connectionFactory;
                    }
                    if (arguments.equals(MQHealthListener.class)) {
                        return healthListener;
                    }
                    return null;
                });
        // Listener mocks
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession()).thenReturn(session);
        when(session.createTemporaryQueue()).thenReturn(queue);
        // Sender Mock
        when(sender.send(any(Destination.class), any(MQMessageCreator.class))).thenReturn(Mono.empty());
        // Act
        TestCustomAnnotation bean = (TestCustomAnnotation) factoryBean.getObject();
        // Assert
        assertNotNull(bean);
        StepVerifier.create(bean.requestReply("Sample"))
                .verifyComplete();
    }
}

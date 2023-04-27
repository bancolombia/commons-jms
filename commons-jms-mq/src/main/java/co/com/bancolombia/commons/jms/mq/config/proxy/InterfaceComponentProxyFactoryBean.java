package co.com.bancolombia.commons.jms.mq.config.proxy;


import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.listeners.MQRequestReplyListener;
import co.com.bancolombia.commons.jms.utils.MQMessageListenerUtils;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import com.ibm.mq.jakarta.jms.MQQueue;
import jakarta.jms.Queue;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.Message;
import java.lang.reflect.Method;
import java.util.Arrays;

import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveConcurrency;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveQueue;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveRetries;
import static org.springframework.util.ClassUtils.resolveClassName;

@Log4j2
public class InterfaceComponentProxyFactoryBean implements FactoryBean<Object>, BeanFactoryAware {
    private final AnnotationMetadata metadata;
    private final Class<?> objectType;
    private BeanFactory beanFactory;
    private EmbeddedValueResolver embeddedValueResolver;

    public InterfaceComponentProxyFactoryBean(AnnotationMetadata metadata) {
        this.metadata = metadata;
        objectType = resolveClassName(metadata.getClassName(), null);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    @Override
    public Object getObject() {
        log.info("Creating req reply bean for {}", getObjectType());
        if (getObjectType() != null && !InterfaceComponentProxyFactoryBean.class.getClassLoader().equals(getObjectType().getClassLoader())) {
            log.warn("Your class loader has been changed, please add System.setProperty(\"spring.devtools.restart.enabled\", \"false\"); before SpringApplication.run(...)");
        }
        return Proxy.newProxyInstance(InterfaceComponentProxyFactoryBean.class.getClassLoader(),
                new Class[]{getObjectType()},
                new Target(getObjectType(), buildInstance()));
    }

    private MQRequestReply buildInstance() {
        MQMessageSender sender = beanFactory.getBean(MQMessageSender.class);
        ReactiveReplyRouter<Message> router = resolveReplier();
        MQQueuesContainer container = beanFactory.getBean(MQQueuesContainer.class);
        String className = ClassUtils.getShortName(metadata.getClassName());
        MQProperties properties = resolveBeanWithName("", MQProperties.class);
        MergedAnnotation<ReqReply> annotation = metadata.getAnnotations().get(ReqReply.class);
        MQListenerConfig config = validateAnnotationConfig(annotation, properties, className);
        ConnectionFactory cf = resolveBeanWithName(config.getConnectionFactory(), ConnectionFactory.class);
        MQBrokerUtils mqBrokerUtils = beanFactory.getBean(MQBrokerUtils.class);
        MQHealthListener healthListener = beanFactory.getBean(MQHealthListener.class);

        Destination destination = resolveDestination(annotation, properties);

        MQRequestReplyListener senderWithRouter = new MQRequestReplyListener(sender, router, container, destination,
                config.getTempQueueAlias(), config.getMaxRetries());

        RetryableConfig retryableConfig = RetryableConfig.builder()
                .maxRetries(properties.getMaxRetries())
                .initialRetryIntervalMillis(properties.getInitialRetryIntervalMillis())
                .multiplier(properties.getRetryMultiplier())
                .build();
        try {
            MQMessageListenerUtils.createListeners(cf, senderWithRouter, container, mqBrokerUtils, config, healthListener,
                    retryableConfig);
        } catch (JMSRuntimeException ex) {
            throw new BeanInitializationException("Could not create request reply defined in " + className
                    + " connectionFactory: " + cf, ex);
        }

        return senderWithRouter;
    }

    @SneakyThrows
    private Destination resolveDestination(MergedAnnotation<ReqReply> annotation, MQProperties properties) {
        String requestQueue = resolveValueFromAnnotation(annotation, "requestQueue");
        String name = StringUtils.hasText(requestQueue) ? requestQueue : properties.getOutputQueue();
        MQQueueCustomizer customizer = beanFactory.getBean(MQQueueCustomizer.class);
        Queue queue = new MQQueue(name);
        customizer.customize(queue);
        return queue;
    }

    private MQListenerConfig validateAnnotationConfig(MergedAnnotation<ReqReply> annotation, MQProperties properties,
                                                      String className) {
        // Annotation property
        String concurrencyAnnotation = resolveValueFromAnnotation(annotation, "concurrency");
        String queueCustomizerAnnotation = resolveValueFromAnnotation(annotation, "queueCustomizer");
        String replyQueueTempAnnotation = resolveValueFromAnnotation(annotation, "replyQueueTemp");
        String maxRetriesAnnotation = resolveValueFromAnnotation(annotation, "maxRetries");
        String connectionFactoryAnnotation = resolveValueFromAnnotation(annotation, "connectionFactory");

        // Resolve dynamic values
        int concurrency = Integer.parseInt(concurrencyAnnotation);
        MQQueueCustomizer customizer = resolveBeanWithName(queueCustomizerAnnotation, MQQueueCustomizer.class);
        String temporaryQueue = resolveQueue(replyQueueTempAnnotation, "", className);
        int finalConcurrency = resolveConcurrency(concurrency, properties.getInputConcurrency());
        int maxRetries = resolveRetries(maxRetriesAnnotation);
        MQListenerConfig listenerConfig = MQListenerConfig.builder()
                .concurrency(finalConcurrency)
                .tempQueueAlias(temporaryQueue)
                .queue("")
                .connectionFactory(connectionFactoryAnnotation)
                .customizer(customizer)
                .maxRetries(maxRetries)
                .build();
        if (!StringUtils.hasText(listenerConfig.getQueue()) && !StringUtils.hasText(listenerConfig.getTempQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define one of value or tempQueueAlias");
        }
        return listenerConfig;
    }

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    @SuppressWarnings("unchecked")
    private ReactiveReplyRouter<Message> resolveReplier() {
        ResolvableType resolvable = ResolvableType.forClassWithGenerics(ReactiveReplyRouter.class, Message.class);
        return (ReactiveReplyRouter<Message>) beanFactory.getBeanProvider(resolvable)
                .getIfAvailable(ReactiveReplyRouter::new);
    }

    private String resolveValueFromAnnotation(MergedAnnotation<ReqReply> annotation, String property) {
        return embeddedValueResolver.resolveStringValue(annotation.getString(property));
    }

    private <T> T resolveBeanWithName(String beanName, Class<T> tClass) {
        if (StringUtils.hasText(beanName)) {
            return beanFactory.getBean(beanName, tClass);
        } else {
            return beanFactory.getBean(tClass);
        }
    }

    private static class Target implements InvocationHandler {
        private final Method matchedMethod;
        private final Object targetMethod;

        public Target(Class<?> clazz, Object targetMethod) {
            Method required = clazz.getDeclaredMethods()[0];
            Method selected = null;
            for (Method method : targetMethod.getClass().getDeclaredMethods()) {
                if (matches(required, method)) {
                    selected = method;
                    break;
                }
            }
            this.matchedMethod = selected;
            this.targetMethod = targetMethod;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            return matchedMethod.invoke(targetMethod, objects);
        }

        private static boolean matches(Method required, Method method) {
            if (!required.getName().equals(method.getName())) {
                return false;
            }
            if (!required.getReturnType().equals(method.getReturnType())) {
                return false;
            }
            return Arrays.equals(required.getParameterTypes(), method.getParameterTypes());
        }
    }

}


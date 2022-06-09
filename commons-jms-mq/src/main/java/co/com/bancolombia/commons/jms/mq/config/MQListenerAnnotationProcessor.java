package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.commons.jms.mq.MQListeners;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidListenerException;
import co.com.bancolombia.commons.jms.mq.listeners.MQMessageListener;
import co.com.bancolombia.commons.jms.mq.listeners.MQReactiveMessageListener;
import co.com.bancolombia.commons.jms.utils.MQMessageListenerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import javax.jms.ConnectionFactory;
import javax.jms.JMSRuntimeException;
import javax.jms.MessageListener;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveConcurrency;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveQueue;
import static co.com.bancolombia.commons.jms.mq.config.utils.AnnotationUtils.resolveRetries;

@Log4j2
@RequiredArgsConstructor
@Configuration
public class MQListenerAnnotationProcessor implements BeanPostProcessor, BeanFactoryAware {
    private final BeanFactory beanFactory;
    private StringValueResolver embeddedValueResolver;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (AnnotationUtils.isCandidateClass(targetClass, MQListener.class)) {
            Map<Method, Set<MQListener>> annotatedMethods = getAnnotatedMethods(targetClass);
            processAnnotated(bean, beanName, annotatedMethods);
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.embeddedValueResolver = new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory);
        }
    }

    private void processAnnotated(Object bean, String beanName, Map<Method, Set<MQListener>> annotatedMethods) {
        if (!annotatedMethods.isEmpty()) {
            annotatedMethods.forEach((method, listeners) -> listeners.forEach(listener -> processJmsListener(listener, method, bean)));
            if (log.isInfoEnabled()) {
                log.info("{} @MQListener methods processed on bean '{}': {}", annotatedMethods.size(), beanName, annotatedMethods);
            }
        }
    }

    private void processJmsListener(MQListener mqListener, Method mostSpecificMethod, Object bean) {
        MQProperties properties = resolveBeanWithName("", MQProperties.class);
        MQListenerConfig config = validateAnnotationConfig(mqListener, properties);
        Method invocableMethod = AopUtils.selectInvocableMethod(mostSpecificMethod, bean.getClass());
        MessageListener processor = getEffectiveMessageListener(bean, invocableMethod, properties.isReactive(), config);
        ConnectionFactory cf = resolveBeanWithName(mqListener.connectionFactory(), ConnectionFactory.class);
        MQQueuesContainer queuesContainer = beanFactory.getBean(MQQueuesContainer.class);
        MQBrokerUtils mqBrokerUtils = beanFactory.getBean(MQBrokerUtils.class);
        MQHealthListener exceptionListener = beanFactory.getBean(MQHealthListener.class);

        try {
            MQMessageListenerUtils.createListeners(cf, processor, queuesContainer, mqBrokerUtils, config, exceptionListener);
        } catch (JMSRuntimeException ex) {
            throw new BeanInitializationException("Could not register MQ listener on [" + mostSpecificMethod + "], using ConnectionFactory: " + cf, ex);
        }
    }

    private MessageListener getEffectiveMessageListener(Object bean, Method invocableMethod, boolean isReactive, MQListenerConfig config) {
        return isReactive ? MQReactiveMessageListener.fromBeanAndMethod(bean, invocableMethod, config.getMaxRetries()) : MQMessageListener.fromBeanAndMethod(bean, invocableMethod, config.getMaxRetries());
    }

    private MQListenerConfig validateAnnotationConfig(MQListener config, MQProperties properties) {
        // Resolve dynamic values
        int concurrency = Integer.parseInt(Objects.requireNonNull(embeddedValueResolver.resolveStringValue(config.concurrency())));
        String queue = embeddedValueResolver.resolveStringValue(config.value());
        MQQueueCustomizer customizer = resolveBeanWithName(config.queueCustomizer(), MQQueueCustomizer.class);
        // Map params
        if (StringUtils.hasText(queue) && StringUtils.hasText(config.tempQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define only one of value or " + "tempQueueAlias in @MQListener annotation");
        }
        if (StringUtils.hasText(properties.getInputQueue()) && StringUtils.hasText(properties.getInputQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define only one of " + "commons.jms.input-queue or commons.jms.input-queue-alias in your application.yaml file");
        }
        String temporaryQueue = resolveQueue(config.tempQueueAlias(), queue, properties.getInputQueueAlias());
        String fixedQueue = resolveQueue(queue, temporaryQueue, properties.getInputQueue());
        int finalConcurrency = resolveConcurrency(concurrency, properties.getInputConcurrency());
        int maxRetries = resolveRetries(config.maxRetries());
        MQListenerConfig listenerConfig = MQListenerConfig.builder().concurrency(finalConcurrency).tempQueueAlias(temporaryQueue).queue(fixedQueue).connectionFactory(config.connectionFactory()).customizer(customizer).maxRetries(maxRetries).build();
        if (!StringUtils.hasText(listenerConfig.getQueue()) && !StringUtils.hasText(listenerConfig.getTempQueueAlias())) {
            throw new MQInvalidListenerException("Invalid configuration, should define one of value or tempQueueAlias");
        }
        return listenerConfig;
    }

    private Map<Method, Set<MQListener>> getAnnotatedMethods(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass, (MethodIntrospector.MetadataLookup<Set<MQListener>>) method -> {
            Set<MQListener> listenerMethods = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, MQListener.class, MQListeners.class);
            return (!listenerMethods.isEmpty() ? listenerMethods : null);
        });
    }

    private <T> T resolveBeanWithName(String beanName, Class<T> tClass) {
        if (StringUtils.hasText(beanName)) {
            return beanFactory.getBean(beanName, tClass);
        } else {
            return beanFactory.getBean(tClass);
        }
    }

}

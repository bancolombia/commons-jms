package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueManagerSetter;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.MQExecutorService;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorBuilder;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.config.senders.MQSenderContainer;
import co.com.bancolombia.commons.jms.utils.ReactiveReplyRouter;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

@Component
@RequiredArgsConstructor
public class MQSpringResolver implements EmbeddedValueResolverAware {
    private final BeanFactory beanFactory;
    @Getter
    private StringValueResolver resolver;

    public <T> T resolveBean(String name, Class<T> clazz) {
        if (StringUtils.hasText(name)) {
            return beanFactory.getBean(name, clazz);
        } else {
            return resolveBean(clazz);
        }
    }

    public <T> T resolveBean(Class<T> clazz) {
        return beanFactory.getBean(clazz);
    }

    public <T> ObjectProvider<T> getProvider(ResolvableType type) {
        return beanFactory.getBeanProvider(type);
    }

    public ConnectionFactory getConnectionFactory(String name) {
        return resolveBean(name, ConnectionFactory.class);
    }

    public MQQueuesContainer getQueuesContainer() {
        return beanFactory.getBean(MQQueuesContainer.class);
    }

    public MQProperties getProperties() {
        return beanFactory.getBean(MQProperties.class);
    }

    public MQBrokerUtils getBrokerUtils() {
        return beanFactory.getBean(MQBrokerUtils.class);
    }

    public MQHealthListener getHealthListener() {
        return beanFactory.getBean(MQHealthListener.class);
    }

    public RetryableConfig getRetryableConfig() {
        return beanFactory.getBean(RetryableConfig.class);
    }

    public MQExecutorService getMqExecutorService() {
        return beanFactory.getBean(MQExecutorService.class);
    }

    public MQQueueManagerSetter getMqQueueManagerSetter() {
        return beanFactory.getBean(MQQueueManagerSetter.class);
    }

    public MQSenderContainer getMqSenderContainer() {
        return beanFactory.getBean(MQSenderContainer.class);
    }

    public SelectorBuilder getSelectorBuilder() {
        return beanFactory.getBean(SelectorBuilder.class);
    }

    public String resolveString(String value) {
        return getResolver().resolveStringValue(value);
    }

    public ReactiveReplyRouter<Message> resolveReplier() {
        return resolveReplier(Message.class);
    }

    @SuppressWarnings("unchecked")
    public <T> ReactiveReplyRouter<T> resolveReplier(Class<T> clazz) {
        ResolvableType resolvable = ResolvableType.forClassWithGenerics(ReactiveReplyRouter.class, clazz);
        return (ReactiveReplyRouter<T>) getProvider(resolvable).getIfAvailable(ReactiveReplyRouter::new);
    }

    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver resolver) {
        this.resolver = resolver;
    }
}

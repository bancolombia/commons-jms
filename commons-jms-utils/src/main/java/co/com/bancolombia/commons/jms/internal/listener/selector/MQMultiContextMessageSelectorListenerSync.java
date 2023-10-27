package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.Message;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MQMultiContextMessageSelectorListenerSync implements MQMessageSelectorListenerSync {
    private final ConnectionFactory connectionFactory;
    private final MQListenerConfig config;
    private final MQHealthListener healthListener;
    private List<MQContextMessageSelectorListenerSync> adapterList;
    private final RetryableConfig retryableConfig;
    private final SelectorModeProvider selectorModeProvider;

    public MQMultiContextMessageSelectorListenerSync(ConnectionFactory connectionFactory, MQListenerConfig config,
                                                     MQHealthListener healthListener, RetryableConfig retryableConfig, SelectorModeProvider selectorModeProvider) {
        this.connectionFactory = connectionFactory;
        this.config = config;
        this.healthListener = healthListener;
        this.retryableConfig = retryableConfig;
        this.selectorModeProvider = selectorModeProvider;
        start();
    }

    public void start() {
        adapterList = IntStream.range(0, config.getConcurrency())
                .mapToObj(idx -> MQContextMessageSelectorListenerSync.builder()
                        .connectionFactory(connectionFactory)
                        .config(config)
                        .healthListener(healthListener)
                        .retryableConfig(retryableConfig)
                        .selectorModeProvider(selectorModeProvider)
                        .build()
                        .call())
                .collect(Collectors.toList());
    }

    public Message getMessage(String correlationId) {
        return getRandom().getMessage(correlationId);
    }

    @Override
    public Message getMessage(String correlationId, long timeout) {
        return getRandom().getMessage(correlationId, timeout);
    }

    public Message getMessage(String correlationId, long timeout, Destination destination) {
        return getRandom().getMessage(correlationId, timeout, destination);
    }

    @Override
    public Message getMessageBySelector(String selector) {
        return getRandom().getMessageBySelector(selector);
    }

    @Override
    public Message getMessageBySelector(String selector, long timeout) {
        return getRandom().getMessageBySelector(selector, timeout);
    }

    @Override
    public Message getMessageBySelector(String selector, long timeout, Destination destination) {
        return getRandom().getMessageBySelector(selector, timeout, destination);
    }

    protected MQMessageSelectorListenerSync getRandom() {
        int selectIndex = (int) (System.currentTimeMillis() % config.getConcurrency());
        return adapterList.get(selectIndex);
    }

}

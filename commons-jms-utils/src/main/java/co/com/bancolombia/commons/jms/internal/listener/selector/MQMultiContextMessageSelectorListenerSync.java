package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.selector.strategy.SelectorModeProvider;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.Destination;
import jakarta.jms.Message;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MQMultiContextMessageSelectorListenerSync implements MQMessageSelectorListenerSync {
    private final int concurrency;
    private final List<MQContextMessageSelectorListenerSync> adapterList;

    public MQMultiContextMessageSelectorListenerSync(MQListenerConfig config,
                                                     MQHealthListener healthListener, RetryableConfig retryableConfig,
                                                     SelectorModeProvider selectorModeProvider,
                                                     MQQueuesContainer container) {
        this.concurrency = config.getConcurrency();
        adapterList = IntStream.range(0, config.getConcurrency())
                .mapToObj(idx -> MQContextMessageSelectorListenerSync.builder()
                        .config(config)
                        .healthListener(healthListener)
                        .retryableConfig(retryableConfig)
                        .selectorModeProvider(selectorModeProvider)
                        .container(container)
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
        int selectIndex = (int) (System.currentTimeMillis() % concurrency);
        return adapterList.get(selectIndex);
    }

}

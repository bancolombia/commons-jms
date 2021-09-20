package co.com.bancolombia.commons.jms.internal.listener.selector;

import co.com.bancolombia.commons.jms.api.MQMessageSelectorListenerSync;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MQMultiContextMessageSelectorListenerSync implements MQMessageSelectorListenerSync {
    private final ConnectionFactory connectionFactory;
    private final MQListenerConfig config;
    private List<MQContextMessageSelectorListenerSync> adapterList;

    public MQMultiContextMessageSelectorListenerSync(ConnectionFactory connectionFactory, MQListenerConfig config) {
        this.connectionFactory = connectionFactory;
        this.config = config;
        start();
    }

    public void start() {
        adapterList = IntStream.range(0, config.getConcurrency())
                .mapToObj(idx -> new MQContextMessageSelectorListenerSync(connectionFactory, config))
                .collect(Collectors.toList());
    }

    public Message getMessage(String correlationId) {
        int selectIndex = (int) (System.currentTimeMillis() % config.getConcurrency());
        return adapterList.get(selectIndex).getMessage(correlationId);
    }

    public Message getMessage(String correlationId, long timeout, Destination destination) {
        int selectIndex = (int) (System.currentTimeMillis() % config.getConcurrency());
        return adapterList.get(selectIndex).getMessage(correlationId, timeout, destination);
    }

}

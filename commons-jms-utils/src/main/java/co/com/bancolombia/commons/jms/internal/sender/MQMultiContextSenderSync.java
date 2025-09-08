package co.com.bancolombia.commons.jms.internal.sender;

import co.com.bancolombia.commons.jms.api.MQMessageCreator;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.internal.models.MQSenderConfig;
import jakarta.jms.Destination;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class MQMultiContextSenderSync implements MQMessageSenderSync {
    private final List<MQMessageSenderSync> adapterList;
    private final int connections;

    public MQMultiContextSenderSync(MQSenderConfig senderConfig) {
        this.connections = senderConfig.getConcurrency();
        if (log.isInfoEnabled()) {
            log.info("Generating {} senders", connections);
        }
        adapterList = IntStream.range(0, senderConfig.getConcurrency())
                .mapToObj(idx -> MQContextSenderSync.builder()
                        .senderConfig(senderConfig)
                        .healthListener(senderConfig.getHealthListener())
                        .retryableConfig(senderConfig.getRetryableConfig())
                        .build()
                        .call())
                .collect(Collectors.toList());
    }

    @Override
    public String send(String message) {
        return checkout().send(message);
    }

    @Override
    public String send(MQMessageCreator messageCreator) {
        return checkout().send(messageCreator);
    }

    @Override
    public String send(String destination, String message) {
        return checkout().send(destination, message);
    }

    @Override
    public String send(String destination, MQMessageCreator messageCreator) {
        return checkout().send(destination, messageCreator);
    }

    @Override
    public String send(Destination destination, String message) {
        return checkout().send(destination, message);
    }

    @Override
    public String send(Destination destination, MQMessageCreator messageCreator) {
        return checkout().send(destination, messageCreator);
    }

    private MQMessageSenderSync checkout() {
        int selectIndex = (int) (System.currentTimeMillis() % connections);
        return adapterList.get(selectIndex);
    }
}

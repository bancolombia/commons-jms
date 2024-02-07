package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.MQContextListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQMessageListenerUtils {

    public static void createListeners(MQListenerConfig config,
                                       MQQueuesContainer container,
                                       MQBrokerUtils utils,
                                       MQHealthListener healthListener,
                                       RetryableConfig retryableConfig) {
        if (log.isInfoEnabled()) {
            log.info("Creating {} listeners", config.getConcurrency());
        }
        ExecutorService service = Executors.newFixedThreadPool(config.getConcurrency());
        IntStream.range(0, config.getConcurrency())
                .mapToObj(number -> MQContextListener.builder()
                        .listenerConfig(config)
                        .container(container)
                        .utils(utils)
                        .healthListener(healthListener)
                        .retryableConfig(retryableConfig)
                        .build())
                .forEach(service::submit);
        if (log.isInfoEnabled()) {
            log.info("{} listeners created for {}", config.getConcurrency(), config.getListeningQueue());
        }
    }

}

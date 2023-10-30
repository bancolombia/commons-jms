package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.MQContextListener;
import co.com.bancolombia.commons.jms.internal.listener.MQContextTemporaryListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageListener;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQMessageListenerUtils {

    public static void createListeners(ConnectionFactory cf,
                                       MessageListener listener,
                                       MQQueuesContainer container,
                                       MQBrokerUtils utils,
                                       MQListenerConfig config,
                                       MQHealthListener healthListener,
                                       RetryableConfig retryableConfig) {
        if (log.isInfoEnabled()) {
            log.info("Creating {} listeners", config.getConcurrency());
        }
        if (StringUtils.isNotBlank(config.getTempQueueAlias())) {
            createListenersTemp(cf, listener, container, config, healthListener, retryableConfig);
        } else {
            createListenersFixed(cf, listener, container, utils, config, healthListener, retryableConfig);
        }
    }

    private static void createListenersFixed(ConnectionFactory cf,
                                             MessageListener listener,
                                             MQQueuesContainer container,
                                             MQBrokerUtils utils,
                                             MQListenerConfig config,
                                             MQHealthListener healthListener,
                                             RetryableConfig retryableConfig) {
        ExecutorService service = Executors.newFixedThreadPool(config.getConcurrency());
        IntStream.range(0, config.getConcurrency())
                .mapToObj(number -> MQContextListener.builder()
                        .connectionFactory(cf)
                        .utils(utils)
                        .config(config)
                        .listener(listener)
                        .container(container)
                        .healthListener(healthListener)
                        .retryableConfig(retryableConfig)
                        .build())
                .forEach(service::submit);
        if (log.isInfoEnabled()) {
            log.info("{} listeners created for {}", config.getConcurrency(), config.getQueue());
        }
    }

    private static void createListenersTemp(ConnectionFactory cf,
                                            MessageListener listener,
                                            MQQueuesContainer container,
                                            MQListenerConfig config,
                                            MQHealthListener healthListener,
                                            RetryableConfig retryableConfig) {
        ExecutorService service = Executors.newFixedThreadPool(config.getConcurrency());
        IntStream.range(0, config.getConcurrency())
                .mapToObj(number -> MQContextTemporaryListener.builder()
                        .connectionFactory(cf)
                        .config(config)
                        .listener(listener)
                        .container(container)
                        .healthListener(healthListener)
                        .retryableConfig(retryableConfig)
                        .build())
                .forEach(service::submit);
        if (log.isInfoEnabled()) {
            log.info("{} listeners created for {}", config.getConcurrency(), config.getTempQueueAlias());
        }
    }

}

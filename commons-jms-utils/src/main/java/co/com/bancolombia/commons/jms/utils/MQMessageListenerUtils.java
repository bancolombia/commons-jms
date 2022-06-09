package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.listener.MQContextListener;
import co.com.bancolombia.commons.jms.internal.listener.MQMultiConnectionListener;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
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
                                       MQHealthListener healthListener) {
        if (log.isInfoEnabled()) {
            log.info("Creating {} listeners", config.getConcurrency());
        }
        if (StringUtils.isNotBlank(config.getTempQueueAlias())) {
            createListenersTemp(cf, listener, container, config, healthListener);
        } else {
            createListenersFixed(cf, listener, container, utils, config, healthListener);
        }
    }

    private static void createListenersFixed(ConnectionFactory cf,
                                             MessageListener listener,
                                             MQQueuesContainer container,
                                             MQBrokerUtils utils,
                                             MQListenerConfig config,
                                             MQHealthListener healthListener) {
        ExecutorService service = Executors.newFixedThreadPool(config.getConcurrency());
        IntStream.range(0, config.getConcurrency())
                .mapToObj(number -> MQContextListener.builder()
                        .connectionFactory(cf)
                        .utils(utils)
                        .config(config)
                        .listener(listener)
                        .healthListener(healthListener)
                        .container(container)
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
                                            MQHealthListener healthListener) {
        MQMultiConnectionListener.builder()
                .connectionFactory(cf)
                .config(config)
                .listener(listener)
                .container(container)
                .healthListener(healthListener)
                .build()
                .call();
    }

}

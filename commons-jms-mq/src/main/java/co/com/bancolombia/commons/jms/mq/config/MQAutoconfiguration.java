package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQBrokerUtils;
import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.mq.config.health.MQListenerHealthIndicator;
import co.com.bancolombia.commons.jms.mq.utils.MQUtils;
import co.com.bancolombia.commons.jms.utils.MQQueuesContainerImp;
import co.com.bancolombia.commons.jms.utils.MQTemporaryQueuesContainerImp;
import com.ibm.mq.jms.MQQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_MQMD_READ_ENABLED;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_MQMD_WRITE_ENABLED;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_PUT_ASYNC_ALLOWED_ENABLED;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_READ_AHEAD_ALLOWED_ENABLED;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_TARGET_CLIENT;

@Configuration
public class MQAutoconfiguration {

    @Bean
    @ConditionalOnMissingBean(MQQueueCustomizer.class)
    public MQQueueCustomizer defaultMQQueueCustomizer() {
        return queue -> {
            MQQueue customized = (MQQueue) queue;
            customized.setProperty(WMQ_TARGET_CLIENT, "1");
            customized.setProperty(WMQ_MQMD_READ_ENABLED, "true");
            customized.setProperty(WMQ_MQMD_WRITE_ENABLED, "true");
            customized.setPutAsyncAllowed(WMQ_PUT_ASYNC_ALLOWED_ENABLED);
            customized.setReadAheadAllowed(WMQ_READ_AHEAD_ALLOWED_ENABLED);
        };
    }

    @Bean
    @ConditionalOnMissingBean(MQQueuesContainer.class)
    public MQQueuesContainer defaultMQQueuesContainer() {
        return new MQQueuesContainerImp();
    }

    @Bean
    @ConditionalOnMissingBean(MQTemporaryQueuesContainer.class)
    public MQTemporaryQueuesContainer defaultMQTemporaryQueuesContainer(MQQueuesContainer container) {
        return new MQTemporaryQueuesContainerImp(container);
    }

    @Bean
    @ConditionalOnMissingBean(MQBrokerUtils.class)
    public MQBrokerUtils defaultMqBrokerUtils() {
        return (context, queue) -> {
            String qmName = MQUtils.extractQMName(context);
            MQUtils.setQMName(queue, qmName);
        };
    }

    @Bean
    @ConditionalOnMissingBean(MQHealthListener.class)
    public MQHealthListener jmsConnections() {
        return new MQListenerHealthIndicator();
    }
}

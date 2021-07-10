package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.api.MQQueueCustomizer;
import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import co.com.bancolombia.commons.jms.utils.MQTemporaryQueuesContainerImp;
import com.ibm.mq.jms.MQQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ibm.msg.client.wmq.common.CommonConstants.*;

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
    @ConditionalOnMissingBean(MQTemporaryQueuesContainer.class)
    public MQTemporaryQueuesContainer defaultMQTemporaryQueuesContainer() {
        return new MQTemporaryQueuesContainerImp();
    }
}

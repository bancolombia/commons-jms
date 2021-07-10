package co.com.bancolombia.commons.jms.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "commons.jms")
public class MQProperties {
    private int outputConcurrency = 1;
    private String outputQueue;
    private int inputConcurrency = 1;
    private String inputQueue;
    private String inputQueueAlias;
    private long producerTtl = 0;
    private boolean reactive = false;
}

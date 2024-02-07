package co.com.bancolombia.commons.jms.mq.config.utils.sample;

import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMQGateway(scanBasePackages = "co.com.bancolombia.commons.jms.mq.config.utils.sample")
public class SampleConfig {
}

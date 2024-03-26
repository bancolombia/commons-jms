package co.com.bancolombia.sample.app.config;

import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.internal.models.eda.entry.MQHandlerRegistry;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQRegistryConfig {

    @Bean
    public MQHandlerRegistry mqRegistry(ConnectionFactory factory){
        MQHandlerRegistry registry =  MQHandlerRegistry.registry()
                .withConnectionFactory("QM1")
                .withListener(MQListenerConfig.builder()
                        .listeningQueue("QM1-QUEUE-1")
                        .build())
                .withListener(MQListenerConfig.builder()
                        .listeningQueue("QM1-QUEUE-2")
                        .build())
                .withConnectionFactory("QM2")
                .withListener(MQListenerConfig.builder()
                        .listeningQueue("QM2-QUEUE-1")
                        .build())
                .withListener(MQListenerConfig.builder()
                        .listeningQueue("QM2-QUEUE-2")
                        .build())
                .build();

        return registry;
    }
}

package co.com.bancolombia.remote.app;

import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "co.com.bancolombia")
@EnableMQGateway(scanBasePackages = "co.com.bancolombia")
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
    }
}

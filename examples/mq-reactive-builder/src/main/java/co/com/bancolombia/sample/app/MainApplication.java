package co.com.bancolombia.sample.app;

import co.com.bancolombia.commons.jms.mq.EnableMQClientFromSpec;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//@EnableMQGateway(scanBasePackages = "co.com.bancolombia")

@SpringBootApplication(scanBasePackages = "co.com.bancolombia")
@EnableMQClientFromSpec
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
    }
}

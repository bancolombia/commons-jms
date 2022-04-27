package co.com.bancolombia.commons.jms.mq.config.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReqReplyBeanScannerTest {
    private final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
    private ReqReplyBeanScanner beanScanner;

    @BeforeEach
    void setup() {
        beanScanner = new ReqReplyBeanScanner(registry);
    }

    @Test
    void shouldScanBeans() {
        beanScanner.scan("co.com.bancolombia.commons.jms.mq.config.proxy");
        BeanDefinition definition = registry.getBeanDefinition("request.queue");
        assertEquals("co.com.bancolombia.commons.jms.mq.config.proxy.TestCustomAnnotation",
                ((ScannedGenericBeanDefinition) definition).getMetadata().getClassName());
    }

}


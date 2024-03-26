package co.com.bancolombia.commons.jms.mq.config.utils;

import co.com.bancolombia.commons.jms.mq.ReqReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MQBeanScannerTest {
    private final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
    private MQBeanScanner beanScanner;

    @BeforeEach
    void setup() {
        beanScanner = new MQBeanScanner(registry, ReqReply.class, MQFactoryBean.class);
    }

    @Test
    void shouldScanBeans() {
        beanScanner.scan("co.com.bancolombia.commons.jms.mq.config.utils.sample");
        BeanDefinition definition = registry.getBeanDefinition("testReqReply");
        assertEquals("co.com.bancolombia.commons.jms.mq.config.utils.sample.TestReqReply",
                ((ScannedGenericBeanDefinition) definition).getMetadata().getClassName());
    }

}


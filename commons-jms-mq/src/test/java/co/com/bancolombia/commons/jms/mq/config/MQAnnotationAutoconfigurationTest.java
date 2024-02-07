package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.mq.config.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MQAnnotationAutoconfigurationTest {
    private final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
    private MQAnnotationAutoconfiguration annotationAutoconfiguration;

    @BeforeEach
    void setup() {
        annotationAutoconfiguration = new MQAnnotationAutoconfiguration();
    }

    @Test
    void shouldScan() {
        annotationAutoconfiguration.registerBeanDefinitions(Utils.getMetadataEnableMQGateway(), registry);
        assertTrue(registry.containsBeanDefinition("fixedReqReply"));
        assertTrue(registry.containsBeanDefinition("testReqReply"));
        assertTrue(registry.containsBeanDefinition("sampleSender"));
    }
}

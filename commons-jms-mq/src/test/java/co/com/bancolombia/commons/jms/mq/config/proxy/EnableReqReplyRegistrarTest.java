package co.com.bancolombia.commons.jms.mq.config.proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnableReqReplyRegistrarTest {
    private final BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
    private EnableReqReplyRegistrar registrar;

    @BeforeEach
    void setup() {
        registrar = new EnableReqReplyRegistrar();
    }

    @Test
    void shouldRegisterBeanDefinition() {
        // Arrange
        AnnotationMetadata metadata = Utils.getMetadataEnableReqReply();
        // Act
        registrar.registerBeanDefinitions(metadata, registry);
        // Assert
        BeanDefinition definition = registry.getBeanDefinition("request.queue");
        assertEquals("co.com.bancolombia.commons.jms.mq.config.proxy.TestCustomAnnotation",
                ((ScannedGenericBeanDefinition) definition).getMetadata().getClassName());
    }

}

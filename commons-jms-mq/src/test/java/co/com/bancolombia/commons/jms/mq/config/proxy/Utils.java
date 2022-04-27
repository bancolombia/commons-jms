package co.com.bancolombia.commons.jms.mq.config.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.AnnotationMetadata;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static AnnotationMetadata getMetadataEnableReqReply() {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.scan("co.com.bancolombia.commons.jms.mq.config.proxy");
        BeanDefinition definition = registry.getBeanDefinition("sampleConfig");
        return ((ScannedGenericBeanDefinition) definition).getMetadata();
    }

    public static AnnotationMetadata getMetadataReqReply() {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner scanner = new ReqReplyBeanScanner(registry);
        scanner.scan("co.com.bancolombia.commons.jms.mq.config.proxy");
        BeanDefinition definition = registry.getBeanDefinition("request.queue");
        return ((ScannedGenericBeanDefinition) definition).getMetadata();
    }
}

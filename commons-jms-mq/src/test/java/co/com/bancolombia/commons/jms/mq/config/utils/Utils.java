package co.com.bancolombia.commons.jms.mq.config.utils;

import co.com.bancolombia.commons.jms.mq.ReqReply;
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
    public static AnnotationMetadata getMetadataEnableMQGateway() {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
        scanner.scan("co.com.bancolombia.commons.jms.mq.config.utils.sample");
        BeanDefinition definition = registry.getBeanDefinition("sampleConfig");
        return ((ScannedGenericBeanDefinition) definition).getMetadata();
    }

    public static AnnotationMetadata getMetadataReqReply() {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner scanner = new MQBeanScanner(registry, ReqReply.class, MQFactoryBean.class);
        scanner.scan("co.com.bancolombia.commons.jms.mq.config.utils.sample");
        BeanDefinition definition = registry.getBeanDefinition("testReqReply");
        return ((ScannedGenericBeanDefinition) definition).getMetadata();
    }

    public static AnnotationMetadata getMetadataReqReplyFixed() {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ClassPathBeanDefinitionScanner scanner = new MQBeanScanner(registry, ReqReply.class, MQFactoryBean.class);
        scanner.scan("co.com.bancolombia.commons.jms.mq.config.utils.sample");
        BeanDefinition definition = registry.getBeanDefinition("fixedReqReply");
        return ((ScannedGenericBeanDefinition) definition).getMetadata();
    }
}

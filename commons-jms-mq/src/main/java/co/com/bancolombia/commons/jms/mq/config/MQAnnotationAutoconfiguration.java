package co.com.bancolombia.commons.jms.mq.config;

import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import co.com.bancolombia.commons.jms.mq.MQSender;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.utils.MQBeanScanner;
import co.com.bancolombia.commons.jms.mq.config.utils.MQFactoryBean;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Optional;

@Log4j2
public class MQAnnotationAutoconfiguration implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                        @NonNull BeanDefinitionRegistry registry) {
        String className = importingClassMetadata.getClassName();

        importingClassMetadata
                .getAnnotations()
                .stream(EnableMQGateway.class)
                .findFirst()
                .ifPresent(annotation -> {
                    String basePackage = getBasePackage(className, annotation.getValue("value", String.class));

                    new MQBeanScanner(registry, ReqReply.class, MQFactoryBean.class)
                            .scan(basePackage);
                    new MQBeanScanner(registry, MQSender.class, MQFactoryBean.class)
                            .scan(basePackage);
                });
    }

    private String getBasePackage(String className, Optional<String> annotation) {
        if (annotation.isPresent() && !annotation.get().isEmpty()) {
            return annotation.get();
        }
        return ClassUtils.getPackageName(className);
    }
}

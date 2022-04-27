package co.com.bancolombia.commons.jms.mq.config.proxy;

import co.com.bancolombia.commons.jms.mq.EnableReqReply;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class EnableReqReplyRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata configMetadata, BeanDefinitionRegistry registry) {
        String className = configMetadata.getClassName();

        configMetadata
                .getAnnotations()
                .stream(getAnnotationType())
                .forEach(annotation -> {
                    String basePackage = getBasePackage(className, annotation.getValue("value", String.class));
                    new ReqReplyBeanScanner(registry).scan(basePackage);
                });
    }

    protected Class<? extends Annotation> getAnnotationType() {
        return EnableReqReply.class;
    }

    private String getBasePackage(String className, Optional<String> annotation) {
        if (annotation.isPresent() && !annotation.get().isEmpty()) {
            return annotation.get();
        }
        return ClassUtils.getPackageName(className);
    }
}

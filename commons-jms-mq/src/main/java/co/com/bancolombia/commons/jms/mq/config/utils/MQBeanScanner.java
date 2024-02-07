package co.com.bancolombia.commons.jms.mq.config.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;

@Log4j2
public class MQBeanScanner extends ClassPathBeanDefinitionScanner {
    private final Class<?> factoryClass;
    private final Class<?> annotationClass;

    public MQBeanScanner(BeanDefinitionRegistry registry,
                         Class<? extends Annotation> annotationClass, Class<?> factoryClass) {
        super(registry, false);
        this.factoryClass = factoryClass;
        this.annotationClass = annotationClass;
        addIncludeFilter(new AnnotationTypeFilter(annotationClass));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && !metadata.isAnnotation();
    }

    @Override
    protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, @NonNull String beanName) {
        log.info("Defining bean factory for {}", beanName);
        beanDefinition.setBeanClassName(factoryClass.getName());
        beanDefinition.getConstructorArgumentValues()
                .addGenericArgumentValue(((AnnotatedBeanDefinition) beanDefinition).getMetadata());
        beanDefinition.getConstructorArgumentValues()
                .addGenericArgumentValue(annotationClass);
    }
}

package co.com.bancolombia.commons.jms.mq.config.proxy;

import co.com.bancolombia.commons.jms.mq.ReqReply;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

public class ReqReplyBeanScanner extends ClassPathBeanDefinitionScanner {
    public ReqReplyBeanScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        addIncludeFilter(new AnnotationTypeFilter(ReqReply.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface() && !metadata.isAnnotation();
    }

    @Override
    protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
        beanDefinition.setBeanClassName(getFactoryBeanClassName());

        beanDefinition.getConstructorArgumentValues()
                .addGenericArgumentValue(((AnnotatedBeanDefinition) beanDefinition).getMetadata());
    }

    protected String getFactoryBeanClassName() {
        return InterfaceComponentProxyFactoryBean.class.getName();
    }
}

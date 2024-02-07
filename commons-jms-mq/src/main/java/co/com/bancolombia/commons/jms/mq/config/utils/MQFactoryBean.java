package co.com.bancolombia.commons.jms.mq.config.utils;

import co.com.bancolombia.commons.jms.mq.MQSender;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.factory.MQReqReplyFactory;
import co.com.bancolombia.commons.jms.mq.config.factory.MQSenderFactory;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;

import static co.com.bancolombia.commons.jms.mq.config.MQAutoconfiguration.CLASS_LOADER_WARN;
import static org.springframework.util.ClassUtils.resolveClassName;

@Log4j2
@AllArgsConstructor
public class MQFactoryBean<T extends Annotation> implements FactoryBean<Object> {
    private final AnnotationMetadata metadata;
    private final Class<T> annotationClazz;
    private final MQSpringResolver resolver;

    @Override
    public Object getObject() {
        Class<?> clazz = getObjectType();
        if (!MQFactoryBean.class.getClassLoader().equals(clazz.getClassLoader())) {
            log.warn(CLASS_LOADER_WARN);
        }
        log.info("Creating bean for {} annotated with @{}", clazz.getSimpleName(), annotationClazz.getSimpleName());
        MergedAnnotation<T> mergedAnnotation = metadata.getAnnotations().get(annotationClazz);
        T annotation = AnnotationParser.parseMergedAnnotation(mergedAnnotation);
        Object target = build(annotation, clazz);
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        factoryBean.setTarget(target);
        factoryBean.setInterfaces(clazz);
        return factoryBean.getObject();
    }

    @Override
    @NonNull
    public Class<?> getObjectType() {
        return resolveClassName(metadata.getClassName(), null);
    }

    private Object build(T annotation, Class<?> clazz) {
        if (annotation instanceof MQSender) {
            return MQSenderFactory.fromMQSender((MQSender) annotation, resolver, clazz.getSimpleName());
        } else {
            return MQReqReplyFactory.createMQReqReply((ReqReply) annotation, resolver, clazz.getSimpleName());
        }
    }
}

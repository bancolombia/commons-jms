package co.com.bancolombia.commons.jms.mq.config.utils;

import lombok.experimental.UtilityClass;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

@UtilityClass
public class AnnotationParser {

    public static <T extends Annotation> T parseMergedAnnotation(MergedAnnotation<T> mergedAnnotation) {
        Map<String, Object> attributes = mergedAnnotation.asMap(Adapt.ANNOTATION_TO_MAP);
        return createAnnotationProxy(mergedAnnotation.getType(), attributes);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T createAnnotationProxy(Class<T> annotationType, Map<String, Object> attributes) {
        return (T) Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class<?>[]{annotationType},
                new AnnotationInvocationHandler(attributes)
        );
    }

    private static class AnnotationInvocationHandler implements InvocationHandler {
        private final Map<String, Object> attributes;

        AnnotationInvocationHandler(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return attributes.get(method.getName());
        }

    }
}

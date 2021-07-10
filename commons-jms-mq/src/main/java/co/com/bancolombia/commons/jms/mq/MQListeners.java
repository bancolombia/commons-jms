package co.com.bancolombia.commons.jms.mq;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MQListeners {
    MQListener[] value();
}

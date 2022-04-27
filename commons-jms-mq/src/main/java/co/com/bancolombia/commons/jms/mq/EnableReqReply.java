package co.com.bancolombia.commons.jms.mq;

import co.com.bancolombia.commons.jms.mq.config.MQAutoconfigurationSender;
import co.com.bancolombia.commons.jms.mq.config.proxy.EnableReqReplyRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MQAutoconfigurationSender.class, EnableReqReplyRegistrar.class})
public @interface EnableReqReply {

    @AliasFor("scanBasePackages")
    String value() default "";

    @AliasFor("value")
    String scanBasePackages() default "";
}

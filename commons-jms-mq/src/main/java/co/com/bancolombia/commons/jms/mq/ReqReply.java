package co.com.bancolombia.commons.jms.mq;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface ReqReply {

    /**
     * Queue for request
     *
     * @return Queue Name
     */
    @AliasFor("requestQueue")
    String value() default "";

    /**
     * @return Amount of connections to mq
     */
    String concurrency() default "0";

    /**
     * Connection Factory for listening context
     *
     * @return bean name
     * default empty and uses available ConnectionFactory.class bean
     */
    String connectionFactory() default "";

    /**
     * Queue for listening
     *
     * @return Queue Name
     */
    @AliasFor("value")
    String requestQueue() default "";

    /**
     * Alias to register a temporary queue in the MQContainer bean
     *
     * @return temporary queue alias
     * default empty and uses value() for listen a fixed queue
     */
    String replyQueueTemp() default "";

    /**
     * Queue Customizer for listening queue
     *
     * @return bean name
     * default empty and uses available MQQueueCustomizer.class bean
     */
    String queueCustomizer() default "";

    /**
     * Max message processing retries when error handled
     *
     * @return max retries, specify a negative value for infinite retries
     */
    String maxRetries() default "10";
}

package co.com.bancolombia.commons.jms.mq;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation only works for Fixed Queues
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MQListeners.class)
public @interface MQListener {

    /**
     * Queue for listening
     *
     * @return Queue Name
     */
    @AliasFor("listeningQueue")
    String value() default "";

    /**
     * Listening queue name
     * @return queue name
     */
    @AliasFor("value")
    String listeningQueue() default "";

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

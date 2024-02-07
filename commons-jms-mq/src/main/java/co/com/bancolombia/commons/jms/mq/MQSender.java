package co.com.bancolombia.commons.jms.mq;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation only works for Fixed Queues
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MQSender {

    /**
     * Default queue to send
     *
     * @return Queue Name
     */
    @AliasFor("destinationQueue")
    String value() default "";

    @AliasFor("value")
    String destinationQueue() default "";

    /**
     * @return Amount of connections to mq
     */
    String concurrency() default "0";

    /**
     * Connection Factory for listening context
     *
     * @return bean name
     * default empty and uses available {@link jakarta.jms.ConnectionFactory} bean
     */
    String connectionFactory() default "";

    /**
     * Queue Customizer for listening queue
     *
     * @return bean name
     * default empty and uses available {@link co.com.bancolombia.commons.jms.api.MQQueueCustomizer} bean
     */
    String queueCustomizer() default "";

    /**
     * Queue Customizer for listening queue
     *
     * @return bean name
     * default empty and uses available {@link co.com.bancolombia.commons.jms.api.MQProducerCustomizer} bean
     */
    String producerCustomizer() default "";

    /**
     * Retry config
     *
     * @return max retries, specify a negative value for infinite retries
     */
    String retryConfig() default "";
}

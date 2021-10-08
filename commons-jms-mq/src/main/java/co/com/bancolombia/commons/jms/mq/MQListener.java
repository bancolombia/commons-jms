package co.com.bancolombia.commons.jms.mq;

import java.lang.annotation.*;

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
     * Alias to register a temporary queue in the MQContainer bean
     *
     * @return temporary queue alias
     * default empty and uses value() for listen a fixed queue
     */
    String tempQueueAlias() default "";

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

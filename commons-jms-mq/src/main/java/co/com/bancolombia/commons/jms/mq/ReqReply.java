package co.com.bancolombia.commons.jms.mq;

import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqReply {
    // Shared

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

    // Sender

    /**
     * Queue for request
     *
     * @return Queue Name
     */
    @AliasFor(value = "requestQueue")
    String value() default "";

    /**
     * Queue for send request
     *
     * @return Queue Name
     */
    @AliasFor("value")
    String requestQueue() default "";

    /**
     * Queue Customizer for request queue
     *
     * @return bean name
     * default empty and uses available {@link co.com.bancolombia.commons.jms.api.MQQueueCustomizer} bean
     */
    String queueCustomizer() default "";

    /**
     * Queue Customizer for producer queue
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

    // Listener

    /**
     * Alias to register a temporary queue in the MQContainer bean when queueType = TEMPORARY
     * Fixed queue name when queueType = FIXED
     * {@link MQListenerConfig.QueueType}
     *
     * @return temporary queue alias or queue name
     * default empty
     */
    String replyQueue() default "";

    /**
     * Queue Customizer for listening queue
     *
     * @return bean name
     * default empty and uses available {@link co.com.bancolombia.commons.jms.api.MQQueueCustomizer} bean
     */
    String replyQueueCustomizer() default "";

    /**
     * Listener retries when temporary queues
     *
     * @return max retries, specify a negative value for infinite retries
     */
    String maxRetries() default "10";

    /**
     * Type of reply queue: TEMPORARY | FIXED
     * {@link MQListenerConfig.QueueType}
     *
     * @return temporary queue alias
     * default empty
     */
    MQListenerConfig.QueueType queueType() default MQListenerConfig.QueueType.TEMPORARY;

    /**
     * Type of context to retrieve the message
     *
     * @return selector mode: CONTEXT_SHARED | CONTEXT_PER_MESSAGE
     * default CONTEXT_SHARED
     */
    String selectorMode() default "CONTEXT_SHARED";


}

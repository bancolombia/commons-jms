package co.com.bancolombia.commons.jms.mq.config.factory.model;


import co.com.bancolombia.commons.jms.mq.MQSender;
import co.com.bancolombia.commons.jms.mq.ReqReply;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationSenderSettings {
    private final String concurrency;
    private final String destinationQueue;
    private final String connectionFactory;
    private final String queueCustomizer;
    private final String producerCustomizer;
    private final String retryConfig;

    public static AnnotationSenderSettings from(MQSender annotation) {
        return AnnotationSenderSettings.builder()
                .concurrency(annotation.concurrency())
                .destinationQueue(annotation.destinationQueue())
                .connectionFactory(annotation.connectionFactory())
                .queueCustomizer(annotation.queueCustomizer())
                .producerCustomizer(annotation.producerCustomizer())
                .retryConfig(annotation.retryConfig())
                .build();
    }

    public static AnnotationSenderSettings from(ReqReply annotation) {
        return AnnotationSenderSettings.builder()
                .concurrency(annotation.concurrency())
                .destinationQueue(annotation.requestQueue())
                .connectionFactory(annotation.connectionFactory())
                .queueCustomizer(annotation.queueCustomizer())
                .producerCustomizer(annotation.producerCustomizer())
                .retryConfig(annotation.retryConfig())
                .build();
    }
}

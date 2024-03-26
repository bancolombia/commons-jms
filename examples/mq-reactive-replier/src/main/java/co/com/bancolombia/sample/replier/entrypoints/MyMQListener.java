package co.com.bancolombia.sample.replier.entrypoints;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.mq.EnableMQGateway;
import co.com.bancolombia.commons.jms.mq.MQListener;
import co.com.bancolombia.sample.replier.drivenadapters.XDomainSender;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log4j2
@Component
@AllArgsConstructor
@EnableMQGateway(scanBasePackages = "co.com.bancolombia")
public class MyMQListener {
    private final MQMessageSender sender;
//    private final XDomainSender sender2;

    @MQListener
    public Mono<Void> process(Message message) throws JMSException {
        log.info("Received and processing from default domain");
        TextMessage textMessage = (TextMessage) message;
        String id = message.getJMSMessageID();
        log.info("Received with id: {}", id);
        return sender.send(textMessage.getJMSReplyTo(), context -> {
                    TextMessage response = context.createTextMessage(textMessage.getText() + " replied default domain");
                    response.setJMSCorrelationID(textMessage.getJMSMessageID());
                    return response;
                }).then()
                .delaySubscription(Duration.ofMillis(100)); // Simulates some latency
    }

//    @MQListener(connectionFactory = "domainB")
//    public Mono<Void> processFromDomainB(Message message) throws JMSException {
//        log.info("Received and processing from domainB");
//        TextMessage textMessage = (TextMessage) message;
//        String id = message.getJMSMessageID();
//        log.info("Received with id: {}", id);
//        return sender2.send(textMessage.getJMSReplyTo(), context -> {
//                    TextMessage response = context.createTextMessage(textMessage.getText() + " replied domainB");
//                    response.setJMSCorrelationID(textMessage.getJMSMessageID());
//                    return response;
//                }).then()
//                .delaySubscription(Duration.ofMillis(100)); // Simulates some latency
//    }
}

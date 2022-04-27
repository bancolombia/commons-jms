package co.com.bancolombia.jms.replier.entrypoints;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.mq.EnableMQMessageSender;
import co.com.bancolombia.commons.jms.mq.MQListener;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.time.Duration;

@Log4j2
@Component
@AllArgsConstructor
@EnableMQMessageSender
public class MyMQListener {
    private final MQMessageSender sender;

    @MQListener
    public Mono<Void> process(Message message) throws JMSException {
        log.info("Received and processing");
        TextMessage textMessage = (TextMessage) message;
        String id = message.getJMSMessageID();
        log.info("Received with id: {}", id);
        return sender.send(textMessage.getJMSReplyTo(), context -> {
                    TextMessage response = context.createTextMessage(textMessage.getText() + " replied");
                    response.setJMSCorrelationID(textMessage.getJMSMessageID());
                    return response;
                }).then()
                .delaySubscription(Duration.ofMillis(100)); // Simulates some latency
    }
}

package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class Utils {

    @SneakyThrows
    public static JmsMessage fromMessage(Message message) {
        String body;
        if (message instanceof TextMessage) {
            body = ((TextMessage) message).getText();
        } else if (message instanceof BytesMessage) {
            body = ((BytesMessage) message).readUTF();
        } else {
            log.warn("Message type not identified getting response as String");
            body = message.getBody(String.class);
        }
        return JmsMessage.builder()
                .messageID(message.getJMSMessageID())
                .correlationID(message.getJMSCorrelationID())
                .timestamp(message.getJMSTimestamp())
                .body(body)
                .build();
    }
}

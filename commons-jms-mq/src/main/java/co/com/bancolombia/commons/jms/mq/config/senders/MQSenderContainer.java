package co.com.bancolombia.commons.jms.mq.config.senders;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidSenderException;

import java.util.concurrent.ConcurrentHashMap;

public class MQSenderContainer extends ConcurrentHashMap<String, Object> {
    public MQMessageSender getReactive(String domain) {
        MQMessageSender res = (MQMessageSender) get(domain);
        if (res == null) {
            throw new MQInvalidSenderException("Sender for domain " + domain + " could not be found");
        }
        return res;
    }

    public MQMessageSenderSync getImperative(String domain) {
        MQMessageSenderSync res = (MQMessageSenderSync) get(domain);
        if (res == null) {
            throw new MQInvalidSenderException("Sender for domain " + domain + " could not be found");
        }
        return res;
    }
}

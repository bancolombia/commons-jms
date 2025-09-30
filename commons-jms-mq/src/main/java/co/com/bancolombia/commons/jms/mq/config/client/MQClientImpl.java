package co.com.bancolombia.commons.jms.mq.config.client;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.api.model.MQClient;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidSenderException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MQClientImpl implements MQClient {
    private static final String NOT_FOUND = " not found";
    private final Map<String, MQMessageSender> senders = new HashMap<>();
    private final Map<String, MQRequestReply> fixedRequestReplies = new HashMap<>();
    private final Map<String, MQRequestReply> temporaryRequestReplies = new HashMap<>();

    @Override
    public MQMessageSender sender(String domain) {
        return Optional.of(senders.get(domain))
                .orElseThrow(() -> new MQInvalidSenderException("Sender for domain " + domain + NOT_FOUND));
    }

    @Override
    public MQRequestReply fixedReqReply(String domain) {
        return Optional.of(fixedRequestReplies.get(domain))
                .orElseThrow(() -> new MQInvalidSenderException("Fixed ReqReply for domain " + domain + NOT_FOUND));
    }

    @Override
    public MQRequestReply temporaryReqReply(String domain) {
        return Optional.of(temporaryRequestReplies.get(domain))
                .orElseThrow(() -> new MQInvalidSenderException("Temporary ReqReply for domain " + domain + NOT_FOUND));
    }

    public void withSender(String domain, MQMessageSender sender) {
        senders.put(domain, sender);
    }

    public void withFixedRequestReply(String domain, MQRequestReply requestReply) {
        fixedRequestReplies.put(domain, requestReply);
    }

    public void withTemporaryRequestReply(String domain, MQRequestReply requestReply) {
        temporaryRequestReplies.put(domain, requestReply);
    }
}

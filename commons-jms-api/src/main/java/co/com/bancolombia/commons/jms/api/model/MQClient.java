package co.com.bancolombia.commons.jms.api.model;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQRequestReply;

public interface MQClient {
    MQMessageSender sender(String domain);

    MQRequestReply fixedReqReply(String domain);

    MQRequestReply temporaryReqReply(String domain);
}

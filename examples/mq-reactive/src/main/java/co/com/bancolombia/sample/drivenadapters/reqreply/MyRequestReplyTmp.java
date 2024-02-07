package co.com.bancolombia.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.mq.ReqReply;

@ReqReply(requestQueue = "DEV.QUEUE.1", replyQueue = "alias", connectionFactory = "")
public interface MyRequestReplyTmp extends MQRequestReply {
}

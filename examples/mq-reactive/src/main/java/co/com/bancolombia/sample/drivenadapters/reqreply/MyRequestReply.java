package co.com.bancolombia.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.mq.ReqReply;

import static co.com.bancolombia.commons.jms.internal.models.MQListenerConfig.QueueType.FIXED;

@ReqReply(requestQueue = "DEV.QUEUE.1", replyQueue = "DEV.QUEUE.2", queueType = FIXED, selectorMode = "{}", connectionFactory = "domainB")
public interface MyRequestReply extends MQRequestReply {
}

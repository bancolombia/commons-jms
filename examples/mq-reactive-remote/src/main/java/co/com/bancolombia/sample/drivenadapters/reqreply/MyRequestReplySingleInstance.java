package co.com.bancolombia.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.mq.ReqReply;

import static co.com.bancolombia.commons.jms.internal.models.MQListenerConfig.QueueType.FIXED_LOCATION_TRANSPARENCY;

@ReqReply(requestQueue = "DEV.QUEUE.1", replyQueue = "DEV.QUEUE.2", queueType = FIXED_LOCATION_TRANSPARENCY, connectionFactory = "domainB")
public interface MyRequestReplySingleInstance extends MQRequestReply {
}

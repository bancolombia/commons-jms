package co.com.bancolombia.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.mq.ReqReply;

import static co.com.bancolombia.commons.jms.internal.models.MQListenerConfig.QueueType.FIXED;
import static co.com.bancolombia.commons.jms.internal.models.MQListenerConfig.QueueType.FIXED_SINGLE_INSTANCE;

@ReqReply(requestQueue = "DEV.QUEUE.1", replyQueue = "DEV.QUEUE.2", queueType = FIXED_SINGLE_INSTANCE, connectionFactory = "domainB")
public interface MyRequestReplySingleInstance extends MQRequestReply {
}

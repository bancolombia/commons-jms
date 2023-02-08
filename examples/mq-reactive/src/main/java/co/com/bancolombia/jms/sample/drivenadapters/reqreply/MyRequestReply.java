package co.com.bancolombia.jms.sample.drivenadapters.reqreply;

import co.com.bancolombia.commons.jms.mq.ReqReply;
import reactor.core.publisher.Mono;

import jakarta.jms.Message;

@ReqReply(requestQueue = "DEV.QUEUE.1", replyQueueTemp = "sample")
public interface MyRequestReply {
    Mono<Message> requestReply(String message);
}

package co.com.bancolombia.sample.replier.drivenadapters;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.mq.MQSender;

@MQSender(connectionFactory = "domainB")
public interface XDomainSender extends MQMessageSender {
}

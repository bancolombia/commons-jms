package co.com.bancolombia.commons.jms.mq.config.factory;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQRequestReply;
import co.com.bancolombia.commons.jms.api.model.MQClient;
import co.com.bancolombia.commons.jms.api.model.spec.CommonsJMSSpec;
import co.com.bancolombia.commons.jms.api.model.spec.MQDomainSpec;
import co.com.bancolombia.commons.jms.internal.models.MQListenerConfig;
import co.com.bancolombia.commons.jms.mq.config.MQSpringResolver;
import co.com.bancolombia.commons.jms.mq.config.client.MQClientImpl;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MQClientFactory {

    public static void fromSpec(CommonsJMSSpec spec, MQSpringResolver resolver) {
        MQClient client = resolver.resolveBean(MQClient.class);
        spec.getDomains().values()
                .forEach(domainSpec -> {
                    startListeners(domainSpec, resolver);
                    if (domainSpec.isEnableMessageSender()) {
                        createSender(domainSpec, client, resolver);
                    }
                    if (domainSpec.isEnableFixedReqReply()) {
                        createFixedReqReply(domainSpec, client, resolver);
                    }
                    if (domainSpec.isEnableTemporaryReqReply()) {
                        createTemporaryReqReply(domainSpec, client, resolver);
                    }
                });
    }

    private static void startListeners(MQDomainSpec domainSpec, MQSpringResolver resolver) {
        domainSpec.getMessageListeners()
                .forEach((listenerSpec) ->
                        MQListenerFactory.createListener(resolver, domainSpec.getConnectionFactory(), listenerSpec));
    }

    private static void createSender(MQDomainSpec domainSpec, MQClient client, MQSpringResolver resolver) {
        MQMessageSender sender = MQSenderFactory.forConnectionFactory(domainSpec, resolver);
        MQClientImpl implClient = (MQClientImpl) client;
        implClient.withSender(domainSpec.getName(), sender);
    }

    private static void createFixedReqReply(MQDomainSpec domainSpec, MQClient client, MQSpringResolver resolver) {
        MQRequestReply requestReply = MQReqReplyFactory.createMQReqReply(domainSpec,
                client.sender(domainSpec.getName()),
                MQListenerConfig.QueueType.FIXED,
                resolver);
        MQClientImpl implClient = (MQClientImpl) client;
        implClient.withFixedRequestReply(domainSpec.getName(), requestReply);
    }

    private static void createTemporaryReqReply(MQDomainSpec domainSpec, MQClient client, MQSpringResolver resolver) {
        MQRequestReply requestReply = MQReqReplyFactory.createMQReqReply(domainSpec,
                client.sender(domainSpec.getName()),
                MQListenerConfig.QueueType.TEMPORARY,
                resolver);
        MQClientImpl implClient = (MQClientImpl) client;
        implClient.withTemporaryRequestReply(domainSpec.getName(), requestReply);
    }
}

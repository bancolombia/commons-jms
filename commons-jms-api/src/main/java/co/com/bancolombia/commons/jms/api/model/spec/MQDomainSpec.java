package co.com.bancolombia.commons.jms.api.model.spec;

import co.com.bancolombia.commons.jms.api.model.MQMessageHandler;
import jakarta.jms.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class MQDomainSpec {
    private final String name;
    private final ConnectionFactory connectionFactory;
    private final List<MQMessageListenerSpec> messageListeners;
    private final boolean enableMessageSender;
    private final boolean enableFixedReqReply;
    private final boolean enableTemporaryReqReply;

    public static class MQDomainSpecBuilder {
        private final String name;
        private final ConnectionFactory connectionFactory;
        private final List<MQMessageListenerSpec> messageListeners;
        private boolean enableMessageSender = false;
        private boolean enableFixedReqReply = false;
        private boolean enableTemporaryReqReply = false;

        public MQDomainSpecBuilder(String name, ConnectionFactory connectionFactory) {
            this.name = name;
            this.connectionFactory = connectionFactory;
            this.messageListeners = new ArrayList<>();
        }

        public MQDomainSpecBuilder listenQueue(String queueName, MQMessageHandler messageListener) {
            this.messageListeners.add(new MQMessageListenerSpec(queueName, messageListener));
            return this;
        }

        public MQDomainSpecBuilder withSender() {
            this.enableMessageSender = true;
            return this;
        }

        public MQDomainSpecBuilder withFixedRequestReply() {
            this.withSender();
            this.enableFixedReqReply = true;
            return this;
        }

        public MQDomainSpecBuilder withTemporaryRequestReply() {
            this.withSender();
            this.enableTemporaryReqReply = true;
            return this;
        }

        public MQDomainSpec build() {
            return new MQDomainSpec(name, connectionFactory, messageListeners, enableMessageSender,
                    enableFixedReqReply, enableTemporaryReqReply);
        }
    }

    public static MQDomainSpecBuilder builder(String name, ConnectionFactory connectionFactory) {
        return new MQDomainSpecBuilder(name, connectionFactory);
    }
}

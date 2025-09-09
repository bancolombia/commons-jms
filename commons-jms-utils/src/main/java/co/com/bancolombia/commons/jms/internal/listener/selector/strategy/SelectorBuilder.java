package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

public interface SelectorBuilder {
    String buildSelector(String correlationId);

    static SelectorBuilder ofDefaults() {
        return fromJMSCorrelationID();
    }

    static SelectorBuilder fromJMSCorrelationID() {
        return correlationId -> "JMSCorrelationID='" + correlationId + "'";
    }

    static SelectorBuilder fromJMSMessageID() {
        return correlationId -> "JMSMessageID='" + correlationId + "'";
    }
}

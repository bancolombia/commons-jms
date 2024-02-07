package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

public interface SelectorBuilder {
    String buildSelector(String correlationId);

    static SelectorBuilder ofDefaults() {
        return correlationId -> "JMSCorrelationID='" + correlationId + "'";
    }
}

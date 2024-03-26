package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;

public interface SelectorModeProvider {
    SelectorStrategy get(ConnectionFactory factory, JMSContext context);

    static SelectorModeProvider defaultSelector() {
        return (factory, context) -> new ContextSharedStrategy(context);
    }
}

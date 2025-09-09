package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectorBuilderTest {

    @Test
    void shouldBuildSelectorFromJMSCorrelationID() {
        SelectorBuilder builder = SelectorBuilder.fromJMSCorrelationID();
        String selector = builder.buildSelector("12345");
        assertEquals("JMSCorrelationID='12345'", selector);
    }

    @Test
    void shouldBuildSelectorFromJMSMessageID() {
        SelectorBuilder builder = SelectorBuilder.fromJMSMessageID();
        String selector = builder.buildSelector("67890");
        assertEquals("JMSMessageID='67890'", selector);
    }
}

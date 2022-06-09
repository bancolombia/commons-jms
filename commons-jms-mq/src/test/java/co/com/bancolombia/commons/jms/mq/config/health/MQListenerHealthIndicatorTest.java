package co.com.bancolombia.commons.jms.mq.config.health;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import javax.jms.JMSException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MQListenerHealthIndicatorTest {

    private MQHealthListener healthListener;
    private HealthIndicator indicator;

    @BeforeEach
    void setup() {
        healthListener = new MQListenerHealthIndicator();
        indicator = (HealthIndicator) healthListener;
    }

    @Test
    void shouldIndicatesUpWhenUnknown() {
        healthListener.onInit("sample");
        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void shouldIndicatesUp() {
        healthListener.onInit("sample");
        healthListener.onStarted("sample");
        assertEquals(Status.UP, indicator.health().getStatus());
    }

    @Test
    void shouldIndicatesDown() {
        healthListener.onInit("sample");
        healthListener.onStarted("sample");
        healthListener.onException("sample", new JMSException("sample error"));
        assertEquals(Status.DOWN, indicator.health().getStatus());
    }
}

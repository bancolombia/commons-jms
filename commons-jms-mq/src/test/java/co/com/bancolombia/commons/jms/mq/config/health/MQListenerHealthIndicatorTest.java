package co.com.bancolombia.commons.jms.mq.config.health;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MQListenerHealthIndicatorTest {

    private MQHealthListener healthListener;
    private HealthIndicator indicator;
    @Mock
    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setup() {
        healthListener = new MQListenerHealthIndicator(publisher);
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

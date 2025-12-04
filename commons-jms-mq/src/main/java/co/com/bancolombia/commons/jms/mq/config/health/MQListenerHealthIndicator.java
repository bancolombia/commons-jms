package co.com.bancolombia.commons.jms.mq.config.health;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@RequiredArgsConstructor
public class MQListenerHealthIndicator implements HealthIndicator, MQHealthListener {
    private final Map<String, Status> processes = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Health health() {
        Status status = processes.containsValue(Status.DOWN) ? Status.DOWN : Status.UP;
        if (status == Status.DOWN) {
            log.warn("mqListeners health check has failed {}", processes);
        }
        return Health.status(status).withDetails(processes).build();
    }

    @Override
    public void onInit(String listener) {
        processes.put(listener, Status.UNKNOWN);
    }

    @Override
    public void onStarted(String listener) {
        processes.put(listener, Status.UP);
        if (!processes.containsValue(Status.DOWN) && !processes.containsValue(Status.UNKNOWN)) {
            AvailabilityChangeEvent.publish(this.eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
            AvailabilityChangeEvent.publish(this.eventPublisher, this, LivenessState.CORRECT);
        }
    }

    @Override
    public void onException(String listener, JMSException exception) {
        processes.put(listener, Status.DOWN);
        AvailabilityChangeEvent.publish(this.eventPublisher, exception, ReadinessState.REFUSING_TRAFFIC);
        AvailabilityChangeEvent.publish(this.eventPublisher, this, LivenessState.BROKEN);
    }

}

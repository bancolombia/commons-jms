package co.com.bancolombia.commons.jms.mq.config.health;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import jakarta.jms.JMSException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class MQListenerHealthIndicator implements HealthIndicator, MQHealthListener {
    private final Map<String, Status> processes = new ConcurrentHashMap<>();

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
    }

    @Override
    public void onException(String listener, JMSException exception) {
        processes.put(listener, Status.DOWN);
    }
}

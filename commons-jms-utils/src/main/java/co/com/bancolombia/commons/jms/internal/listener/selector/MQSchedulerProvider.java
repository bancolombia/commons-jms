package co.com.bancolombia.commons.jms.internal.listener.selector;

import reactor.core.scheduler.Scheduler;

import java.util.function.Supplier;

public interface MQSchedulerProvider extends Supplier<Scheduler> {
}

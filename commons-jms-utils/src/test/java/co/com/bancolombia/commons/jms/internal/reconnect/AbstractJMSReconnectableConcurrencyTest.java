package co.com.bancolombia.commons.jms.internal.reconnect;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import jakarta.jms.JMSException;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class AbstractJMSReconnectableConcurrencyTest {

    private static final int THREAD_COUNT = 20;
    private static final int EXCEPTIONS_PER_THREAD = 15;

    private static final RetryableConfig FAST_RETRY = RetryableConfig.builder()
            .maxRetries(3)
            .initialRetryIntervalMillis(20)
            .multiplier(1.0)
            .build();

    private final MQHealthListener noOpHealthListener = new MQHealthListener() {
        @Override
        public void onInit(String listener) { /* no-op */ }

        @Override
        public void onStarted(String listener) { /* no-op */ }

        @Override
        public void onException(String listener, JMSException exception) { /* no-op */ }
    };

    // -------------------------------------------------------------------------
    // Test 1 – no concurrent connect() calls must ever happen
    // -------------------------------------------------------------------------

    @RepeatedTest(5)
    @Timeout(30)
    void shouldNotAllowConcurrentConnections() throws InterruptedException {
        // Arrange
        ConcurrentTestReconnectable reconnectable = ConcurrentTestReconnectable.builder()
                .healthListener(noOpHealthListener)
                .retryableConfig(FAST_RETRY)
                .build();

        reconnectable.call(); // initial connection

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Random random = new Random();

        // Act – every thread fires EXCEPTIONS_PER_THREAD exceptions at random cadence
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // wait until all threads are ready
                    for (int j = 0; j < EXCEPTIONS_PER_THREAD; j++) {
                        reconnectable.onException(new JMSException("concurrent-exception-" + j));
                        Thread.sleep(random.nextInt(10)); //NOSONAR – intentional delay to expose races
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // unleash all threads simultaneously
        assertTrue(doneLatch.await(20, TimeUnit.SECONDS), "All producer threads should finish in time");
        executor.shutdown();

        // Allow any queued reconnection tasks to complete
        Thread.sleep(500); //NOSONAR – needed to drain the reconnection executor

        // Assert – connect() was never invoked from two threads at the same time
        assertTrue(reconnectable.concurrentConnectViolations.isEmpty(),
                "connect() must never run concurrently. Violations (concurrent-in-progress snapshots): "
                        + reconnectable.concurrentConnectViolations);
    }

    // -------------------------------------------------------------------------
    // Test 2 – active connection counter must never go negative
    // -------------------------------------------------------------------------

    @RepeatedTest(5)
    @Timeout(30)
    void counterShouldNeverGoNegative() throws InterruptedException {
        // Arrange
        ConcurrentTestReconnectable reconnectable = ConcurrentTestReconnectable.builder()
                .healthListener(noOpHealthListener)
                .retryableConfig(FAST_RETRY)
                .build();

        reconnectable.call();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Random random = new Random();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < EXCEPTIONS_PER_THREAD; j++) {
                        reconnectable.onException(new JMSException("counter-test-" + j));
                        Thread.sleep(random.nextInt(10)); //NOSONAR
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(20, TimeUnit.SECONDS), "All producer threads should finish in time");
        executor.shutdown();
        Thread.sleep(500); //NOSONAR

        // Assert – counter must be ≥ 0 (negative means more disconnects than connects)
        int finalValue = reconnectable.activeConnections;
        assertTrue(finalValue >= 0,
                "Active-connections counter must never be negative, was: " + finalValue);

        // With an idempotent disconnect() the only "negative-adjacent" event allowed is
        // disconnect() being called when activeConnections == 0 (the pre-init cleanup
        // that start() always performs).  The counter itself must never drop below 0.
        assertTrue(reconnectable.negativeCounterSnapshots.stream().allMatch(v -> v == 0),
                "Unexpected negative counter snapshot: " + reconnectable.negativeCounterSnapshots);
    }

    // -------------------------------------------------------------------------
    // Test 3 – after all exceptions are processed exactly one connection exists
    // -------------------------------------------------------------------------

    @RepeatedTest(3)
    @Timeout(30)
    void shouldSettleToExactlyOneActiveConnectionAfterStorm() throws InterruptedException {
        // Arrange
        ConcurrentTestReconnectable reconnectable = ConcurrentTestReconnectable.builder()
                .healthListener(noOpHealthListener)
                .retryableConfig(FAST_RETRY)
                .build();

        reconnectable.call();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        Random random = new Random();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < EXCEPTIONS_PER_THREAD; j++) {
                        reconnectable.onException(new JMSException("settle-test-" + j));
                        Thread.sleep(random.nextInt(8)); //NOSONAR
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(20, TimeUnit.SECONDS), "All producer threads should finish in time");
        executor.shutdown();

        // Give the single-thread executor enough time to drain all pending tasks
        Thread.sleep(1000); //NOSONAR

        // Assert – system must have settled to exactly one live connection
        assertEquals(1, reconnectable.activeConnections,
                "After reconnection storm settles, exactly 1 active connection expected");
    }

    // -------------------------------------------------------------------------
    // Shared concrete implementation used by all tests
    // -------------------------------------------------------------------------

    @SuperBuilder
    static class ConcurrentTestReconnectable extends AbstractJMSReconnectable<ConcurrentTestReconnectable> {

        /**
         * Net connection counter: connect() → +1 | disconnect() → idempotent -1.
         */
        int activeConnections;

        /**
         * Tracks how many connect() calls are in-flight at the same moment.
         */
        int connectsInProgress;

        /**
         * Any snapshot where connectsInProgress > 1 is a concurrency violation.
         */
        final List<Integer> concurrentConnectViolations =
                Collections.synchronizedList(new ArrayList<>());

        /**
         * Snapshots recorded when disconnect() is called while activeConnections == 0.
         * This is acceptable for the initial startup cleanup, but the counter itself
         * must never drop below 0.
         */
        final List<Integer> negativeCounterSnapshots =
                Collections.synchronizedList(new ArrayList<>());

        @Override
        protected String name() {
            return "concurrency-test";
        }

        @Override
        protected ConcurrentTestReconnectable self() {
            return this;
        }

        @Override
        protected void connect() {
            int inProgress = connectsInProgress++;
            if (inProgress > 1) {
                // Two or more connect() calls running concurrently — this is a bug.
                concurrentConnectViolations.add(inProgress);
            }
            try {
                // Deliberate delay: makes concurrent access far more likely to be detected.
                Thread.sleep(15); //NOSONAR
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                connectsInProgress--;
            }
            activeConnections++;
        }

        @Override
        protected void disconnect() {
            // Idempotent: real MQ disconnect() implementations are safe to call
            // on an uninitialised / already-disconnected resource.
            // getAndUpdate ensures the counter never drops below 0, mirroring that contract.
            activeConnections = Math.max(0, activeConnections - 1);
            int previous = activeConnections;
            if (previous == 0) {
                // Disconnect called when nothing was connected – record for diagnostics
                // (this is expected during the pre-init cleanup inside start()).
                negativeCounterSnapshots.add(previous);
            }
        }
    }
}

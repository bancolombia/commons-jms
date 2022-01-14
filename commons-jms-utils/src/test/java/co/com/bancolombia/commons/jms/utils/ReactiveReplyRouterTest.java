package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.exceptions.RelatedMessageNotFoundException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ReactiveReplyRouterTest {
    private final ReactiveReplyRouter<String> router = new ReactiveReplyRouter<>();

    @Test
    void shouldReply() {
        Mono<String> flow = router.wait("123");
        router.reply("123", "result");
        StepVerifier.create(flow).expectNext("result").verifyComplete();
    }

    @Test
    void shouldFailNotFound() {
        assertThrows(RelatedMessageNotFoundException.class, () -> router.reply("1234", "result"));
    }

    @Test
    void shouldHandleTimeout() {
        Mono<String> flow = router.wait("1234", Duration.ZERO);
        router.reply(null, null);
        StepVerifier.create(flow).expectError(TimeoutException.class).verify();
    }
}

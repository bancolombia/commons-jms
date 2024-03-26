package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.exceptions.RelatedMessageNotFoundException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ReactiveReplyRouterTest {

    @Mock
    private Message message;
    private final ReactiveReplyRouter<String> router = new ReactiveReplyRouter<>();

    @Test
    void shouldReply() {
        Mono<String> flow = router.wait("123");
        router.reply("123", "result");
        StepVerifier.create(flow).expectNext("result").verifyComplete();
    }

    @Test
    void shouldFailNotFoundWhenError() {
        router.error(null, new Exception("Some Error"));
        assertThrows(RelatedMessageNotFoundException.class, () -> router.error("1234", new Exception("Some Error")));
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

    @Test
    void shouldHandleErrorBecauseNotImplemented() {
        StepVerifier.create(router.remoteReply("456", message))
                .expectError(UnsupportedOperationException.class)
                .verify();
    }

    @Test
    void shouldHasRecord() {
        router.wait("123");
        assertTrue(router.hasKey("123"));
        assertFalse(router.hasKey("1234"));
    }

}

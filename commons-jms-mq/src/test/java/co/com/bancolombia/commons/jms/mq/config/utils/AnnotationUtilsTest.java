package co.com.bancolombia.commons.jms.mq.config.utils;

import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnnotationUtilsTest {

    @Test
    void shouldResolveRetries() {
        assertEquals(1, AnnotationUtils.resolveRetries("1"));
        assertEquals(0, AnnotationUtils.resolveRetries("0"));
        assertEquals(-1, AnnotationUtils.resolveRetries("-10"));
        assertEquals(20, AnnotationUtils.resolveRetries("20"));
        assertEquals(MQProperties.DEFAULT_MAX_RETRIES, AnnotationUtils.resolveRetries("NO"));
        assertEquals(MQProperties.DEFAULT_MAX_RETRIES, AnnotationUtils.resolveRetries(""));
    }

    @Test
    void shouldResolveConcurrency() {
        assertEquals(1, AnnotationUtils.resolveConcurrency(1, -1));
        assertEquals(2, AnnotationUtils.resolveConcurrency(0, 2));
        assertEquals(MQProperties.DEFAULT_CONCURRENCY, AnnotationUtils.resolveConcurrency(0, 0));
    }

    @Test
    void shouldResolveQueue() {
        assertEquals("Q1", AnnotationUtils.resolve("Q1", "Q2"));
        assertEquals("Q2", AnnotationUtils.resolve("", "Q2"));
        assertEquals("", AnnotationUtils.resolve("", ""));
        assertEquals("", AnnotationUtils.resolve(null, ""));
    }
}

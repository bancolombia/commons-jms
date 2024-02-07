package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetryableTaskTest {
    @Mock
    private Runnable runnable;

    @Test
    void shouldRetry() {
        // Arrange
        doThrow(new RuntimeException("Error")).when(runnable).run();
        RetryableConfig config = RetryableConfig.builder()
                .initialRetryIntervalMillis(10)
                .maxRetries(3)
                .build();
        // Assert
        Assertions.assertThrows(RuntimeException.class, () -> {
            // Act
            RetryableTask.runWithRetries("sample", config, runnable);
        });
        verify(runnable, times(3)).run();
    }
}

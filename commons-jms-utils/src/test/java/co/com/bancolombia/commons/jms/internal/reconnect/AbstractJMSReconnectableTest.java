package co.com.bancolombia.commons.jms.internal.reconnect;

import co.com.bancolombia.commons.jms.api.exceptions.MQExceptionClassifier;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AbstractJMSReconnectableTest {
    @Mock
    private MQHealthListener healthListener;
    AbstractJMSReconnectable<AbstractJMSReconnectableSample> reconnectable;

    @BeforeEach
    void setup() {
        reconnectable = AbstractJMSReconnectableSample.builder()
                .healthListener(healthListener)
                .exceptionClassifier(new MQExceptionClassifier() {
                    @Override
                    public boolean isReconnectable(JMSRuntimeException e) {
                        return true;
                    }

                    @Override
                    public boolean isReconnectable(JMSException e) {
                        return true;
                    }
                })
                .build();
    }

    @Test
    void shouldStart() {
        // Arrange
        // Act
        AbstractJMSReconnectableSample result = reconnectable.call();
        // Assert
        assertEquals(reconnectable, result);
        verify(healthListener, times(1)).onInit("test-name");
        verify(healthListener, times(1)).onStarted("test-name");
    }

    @Test
    void shouldReconnect() {
        // Arrange
        reconnectable.call();
        JMSException exception = new JMSException("sample");
        // Act
        reconnectable.onException(exception);
        // Assert
        verify(healthListener, times(1)).onInit("test-name");
        verify(healthListener, times(1)).onException("test-name", exception);
        verify(healthListener, atLeastOnce()).onStarted("test-name");
    }

    @Test
    void shouldShutdown() {
        // Arrange
        AbstractJMSReconnectable<AbstractJMSReconnectableSample> spied = Mockito.spy(reconnectable);
        // Act
        spied.shutdown();
        // Assert
        verify(spied, times(1)).disconnect();
    }

    @SuperBuilder
    private static class AbstractJMSReconnectableSample extends AbstractJMSReconnectable<AbstractJMSReconnectableSample> {

        @Override
        protected String name() {
            return "test-name";
        }

        @Override
        protected AbstractJMSReconnectableSample self() {
            return this;
        }

        @Override
        protected void connect() {
            // ignored
        }

        @Override
        protected void disconnect() {
            // ignored
        }

    }
}

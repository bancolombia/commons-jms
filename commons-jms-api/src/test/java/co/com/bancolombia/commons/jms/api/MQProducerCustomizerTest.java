package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MQProducerCustomizerTest {

    @Mock
    private JMSProducer producer;

    @Test
    void shouldCallBothCustomizersInOrder() {
        // Arrange
        MQProducerCustomizer first = p -> p.setDeliveryDelay(100L);
        MQProducerCustomizer second = p -> p.setTimeToLive(5000L);

        MQProducerCustomizer combined = first.andThen(second);

        // Act
        combined.customize(producer);

        // Assert
        InOrder inOrder = inOrder(producer);
        inOrder.verify(producer, times(1)).setDeliveryDelay(100L);
        inOrder.verify(producer, times(1)).setTimeToLive(5000L);
    }

    @Test
    void shouldCallFirstCustomizerBeforeSecond() {
        // Arrange
        boolean[] callOrder = {false, false};

        MQProducerCustomizer first = p -> callOrder[0] = true;
        MQProducerCustomizer second = p -> callOrder[1] = callOrder[0]; // only true if first ran before

        MQProducerCustomizer combined = first.andThen(second);

        // Act
        combined.customize(producer);

        // Assert
        assert callOrder[0] : "First customizer should have been called";
        assert callOrder[1] : "Second customizer should have been called after first";
    }

    @Test
    void shouldPropagateExceptionFromFirstCustomizer() {
        // Arrange
        MQProducerCustomizer first = p -> {
            throw new RuntimeException("Error in first");
        };
        MQProducerCustomizer second = p -> p.setTimeToLive(5000L);

        MQProducerCustomizer combined = first.andThen(second);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> combined.customize(producer));
    }

    @Test
    void shouldPropagateExceptionFromSecondCustomizer() {
        // Arrange
        MQProducerCustomizer first = p -> p.setDeliveryDelay(100L);
        MQProducerCustomizer second = p -> {
            throw new RuntimeException("Error in second");
        };

        MQProducerCustomizer combined = first.andThen(second);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> combined.customize(producer));
    }
}


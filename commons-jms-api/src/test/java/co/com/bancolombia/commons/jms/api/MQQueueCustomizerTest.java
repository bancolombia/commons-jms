package co.com.bancolombia.commons.jms.api;

import jakarta.jms.JMSException;
import jakarta.jms.Queue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MQQueueCustomizerTest {

    @Mock
    private Queue queue;

    @Test
    void shouldCallBothCustomizersInOrder() throws JMSException {
        // Arrange
        MQQueueCustomizer first = q -> q.getQueueName();
        MQQueueCustomizer second = q -> q.getQueueName();

        MQQueueCustomizer combined = first.andThen(second);

        // Act
        combined.customize(queue);

        // Assert
        InOrder inOrder = inOrder(queue);
        inOrder.verify(queue, times(2)).getQueueName();
    }

    @Test
    void shouldCallFirstCustomizerBeforeSecond() throws JMSException {
        // Arrange
        boolean[] callOrder = {false, false};

        MQQueueCustomizer first = q -> callOrder[0] = true;
        MQQueueCustomizer second = q -> callOrder[1] = callOrder[0]; // only true if first ran before

        MQQueueCustomizer combined = first.andThen(second);

        // Act
        combined.customize(queue);

        // Assert
        assert callOrder[0] : "First customizer should have been called";
        assert callOrder[1] : "Second customizer should have been called after first";
    }

    @Test
    void shouldPropagateExceptionFromFirstCustomizer() {
        // Arrange
        MQQueueCustomizer first = q -> {
            throw new JMSException("Error in first");
        };
        MQQueueCustomizer second = q -> q.getQueueName();

        MQQueueCustomizer combined = first.andThen(second);

        // Act & Assert
        assertThrows(JMSException.class,
                () -> combined.customize(queue));
    }
}


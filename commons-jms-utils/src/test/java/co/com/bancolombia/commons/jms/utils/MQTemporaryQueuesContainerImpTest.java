package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.TemporaryQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MQTemporaryQueuesContainerImpTest {
    @Mock
    private TemporaryQueue queue;

    @Test
    void shouldSaveAndGet() {
        // Arrange
        MQTemporaryQueuesContainer container = new MQTemporaryQueuesContainerImp();
        String alias = "key";
        // Act
        container.registerTemporaryQueue(alias, queue);
        TemporaryQueue temporaryQueue = container.get(alias);
        // Assert
        assertEquals(queue, temporaryQueue);
    }
}

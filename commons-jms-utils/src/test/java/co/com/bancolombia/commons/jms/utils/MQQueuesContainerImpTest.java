package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import co.com.bancolombia.commons.jms.api.MQTemporaryQueuesContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.Queue;
import javax.jms.TemporaryQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MQTemporaryQueuesContainerImpTest {
    @Mock
    private TemporaryQueue queue;

    @Test
    void shouldSaveAndGet() {
        // Arrange
        MQQueuesContainer containerNew = new MQQueuesContainerImp();
        MQTemporaryQueuesContainer container = new MQTemporaryQueuesContainerImp(containerNew);
        String alias = "key";
        // Act
        container.registerTemporaryQueue(alias, queue);
        TemporaryQueue temporaryQueue = container.get(alias);
        // Assert
        assertEquals(queue, temporaryQueue);
    }
}

@ExtendWith(MockitoExtension.class)
class MQQueuesContainerImpTest {
    @Mock
    private Queue queue;

    @Test
    void shouldSaveAndGet() {
        // Arrange
        MQQueuesContainer container = new MQQueuesContainerImp();
        String alias = "key";
        // Act
        container.registerQueue(alias, queue);
        Queue temporaryQueue = container.get(alias);
        // Assert
        assertEquals(queue, temporaryQueue);
    }
}

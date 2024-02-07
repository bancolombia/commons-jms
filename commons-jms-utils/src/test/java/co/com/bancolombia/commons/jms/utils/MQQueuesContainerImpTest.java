package co.com.bancolombia.commons.jms.utils;

import co.com.bancolombia.commons.jms.api.MQQueuesContainer;
import jakarta.jms.Queue;
import jakarta.jms.TemporaryQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MQTemporaryQueuesContainerImpTest {
    @Mock
    private TemporaryQueue queue;
    private MQQueuesContainer container;

    @BeforeEach
    void setup(){
        container = new MQQueuesContainerImp();
    }

    @Test
    void shouldBePrintable(){
        assertEquals("MQQueuesContainerImp{tempQueues={}, tempQueueGroups={}}", container.toString());
    }

    @Test
    void shouldSaveAndGet() {
        // Arrange
        MQQueuesContainer container = new MQQueuesContainerImp();
        String alias = "key";
        // Act
        container.registerQueue(alias, queue);
        Queue savedQueue = container.get(alias);
        // Assert
        assertEquals(queue, savedQueue);
    }
}

@ExtendWith(MockitoExtension.class)
class MQQueuesContainerImpTest {
    @Mock
    private Queue queue;
    private MQQueuesContainer container;

    @BeforeEach
    void setup(){
        container = new MQQueuesContainerImp();
    }

    @Test
    void shouldSaveAndGet() {
        // Arrange
        String alias = "key";
        // Act
        container.registerQueue(alias, queue);
        Queue temporaryQueue = container.get(alias);
        // Assert
        assertEquals(queue, temporaryQueue);
    }

    @Test
    void shouldSaveAndGetWhenGroup() {
        // Arrange
        String alias = "key";
        // Act
        container.registerToQueueGroup(alias, queue);
        Queue temporaryQueue = container.get(alias);
        // Assert
        assertEquals(queue, temporaryQueue);
    }
}

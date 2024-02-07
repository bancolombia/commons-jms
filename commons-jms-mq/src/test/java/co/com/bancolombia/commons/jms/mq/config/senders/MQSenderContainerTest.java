package co.com.bancolombia.commons.jms.mq.config.senders;

import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.mq.config.exceptions.MQInvalidSenderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static co.com.bancolombia.commons.jms.mq.config.MQProperties.DEFAULT_DOMAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MQSenderContainerTest {
    @Mock
    private MQMessageSender sender;
    @Mock
    private MQMessageSenderSync senderSync;
    private MQSenderContainer senderContainer;

    @BeforeEach
    public void setup() {
        senderContainer = new MQSenderContainer();
        senderContainer.put(DEFAULT_DOMAIN, sender);
        senderContainer.put("other", senderSync);
    }

    @Test
    void shouldRetrieveSync() {
        // Arrange
        // Act
        MQMessageSenderSync senderImperative = senderContainer.getImperative("other");
        // Assert
        assertEquals(senderSync, senderImperative);
    }

    @Test
    void shouldRetrieve() {
        // Arrange
        // Act
        MQMessageSender senderReactive = senderContainer.getReactive(DEFAULT_DOMAIN);
        // Assert
        assertEquals(sender, senderReactive);
    }

    @Test
    void shouldFailWhenNotRetrieveSync() {
        // Arrange
        // Assert
        assertThrows(MQInvalidSenderException.class, () -> {
            // Act
            senderContainer.getImperative("no-existent");
        });
    }

    @Test
    void shouldFailWhenNotRetrieve() {
        // Arrange
        // Assert
        assertThrows(MQInvalidSenderException.class, () -> {
            // Act
            senderContainer.getReactive("no-existent");
        });
    }
}

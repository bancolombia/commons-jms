package co.com.bancolombia.commons.jms.mq.config.senders;

import co.com.bancolombia.commons.jms.api.MQDestinationProvider;
import co.com.bancolombia.commons.jms.api.MQMessageSender;
import co.com.bancolombia.commons.jms.api.MQMessageSenderSync;
import co.com.bancolombia.commons.jms.api.MQProducerCustomizer;
import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import co.com.bancolombia.commons.jms.internal.models.RetryableConfig;
import co.com.bancolombia.commons.jms.mq.config.MQProperties;
import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQAutoconfigurationSenderTest {
    @Mock
    private ConnectionFactory cf;
    @Mock
    private MQDestinationProvider provider;
    @Mock
    private MQProducerCustomizer customizer;
    @Mock
    private MQProperties properties;
    @Mock
    private MQHealthListener healthListener;
    @Mock
    private RetryableConfig retryableConfig;
    @Mock
    private MQSenderContainer container;
    private MQAutoconfigurationSender senderConfiguration;

    @BeforeEach
    public void setup() {
        senderConfiguration = new MQAutoconfigurationSender();
    }

    @Test
    void shouldCreateSenderSync() {
        // Arrange
        // Act
        MQMessageSenderSync sender = senderConfiguration.defaultMQMessageSenderSync(
                cf,
                provider, customizer,
                properties,
                healthListener,
                retryableConfig,
                container
        );
        // Assert
        assertNotNull(sender);
    }

    @Test
    void shouldCreateSenderAsync() {
        // Arrange
        when(properties.isReactive()).thenReturn(true);
        // Act
        MQMessageSender sender = senderConfiguration.defaultMQMessageSender(
                cf,
                provider,
                customizer,
                properties,
                healthListener,
                retryableConfig,
                senderConfiguration.defaultMqSenderContainer()
        );
        // Assert
        assertNotNull(sender);
    }
}

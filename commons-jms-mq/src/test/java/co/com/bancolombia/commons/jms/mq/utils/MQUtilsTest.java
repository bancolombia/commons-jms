package co.com.bancolombia.commons.jms.mq.utils;

import co.com.bancolombia.commons.jms.mq.helper.JmsContextImpl;
import com.ibm.mq.jakarta.jms.MQQueue;
import com.ibm.msg.client.jakarta.jms.JmsReadablePropertyContext;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_RESOLVED_QUEUE_MANAGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MQUtilsTest {
    @Mock
    private JmsReadablePropertyContext propertyContext;
    @Mock
    private MQQueue queue;
    private JMSContext context;

    @BeforeEach
    void setup() {
        context = new JmsContextImpl(propertyContext);
    }

    @Test
    void shouldGetQMNameFromContext() throws JMSException {
        when(propertyContext.getStringProperty(WMQ_RESOLVED_QUEUE_MANAGER)).thenReturn("QM1");
        String name = MQUtils.extractQMName(context);
        assertEquals("QM1", name);
    }

    @Test
    void shouldGetEmptyQMNameFromContextWhenError() {
        String name = MQUtils.extractQMName(mock(JMSContext.class));
        assertEquals("", name);
    }

    @Test
    void shouldSetQueueQMName() throws JMSException {
        MQUtils.setQMName(queue, "QM1");
        verify(queue, times(1)).setBaseQueueManagerName("QM1");
    }

    @Test
    void shouldCatchErrorSettingQMName() throws JMSException {
        doThrow(new JMSException("Error setting QM name")).when(queue).setBaseQueueManagerName("QM1");
        MQUtils.setQMName(queue, "QM1");
        verify(queue, times(1)).setBaseQueueManagerName("QM1");
    }

    @Test
    void shouldExtractQMNameFromTemporaryQueue() {
        TemporaryQueue temporaryQueue = mock(TemporaryQueue.class);
        doReturn("queue://QM1/MY.TMP.QUEUE").when(temporaryQueue).toString();
        JMSContext jmsContext = mock(JMSContext.class);
        doReturn(temporaryQueue).when(jmsContext).createTemporaryQueue();
        String name = MQUtils.extractQMNameWithTempQueue(jmsContext);
        assertEquals("QM1", name);
    }

    @Test
    void shouldCatchErrorQMNameFromTemporaryQueue() {
        JMSContext jmsContext = mock(JMSContext.class);
        doThrow(new JMSRuntimeException("Error creating temporary queue")).when(jmsContext).createTemporaryQueue();
        String name = MQUtils.extractQMNameWithTempQueue(jmsContext);
        assertEquals("", name);
    }

    @Test
    void shouldSetIds() throws JMSException {
        int expectedInvocations = 2;
        TextMessage message = mock(TextMessage.class);
        String id = MQUtils.generateUniqueId();
        MQUtils.setMessageId(message, id);
        MQUtils.setCorrelationId(message, id);
        verify(message, times(expectedInvocations)).setObjectProperty(anyString(), any());
    }
}

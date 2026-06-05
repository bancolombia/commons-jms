package co.com.bancolombia.commons.jms.mq.config.exceptions;

import com.ibm.mq.MQException;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import org.junit.jupiter.api.Test;

import static com.ibm.mq.constants.CMQC.MQRC_Q_FULL;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MQExceptionClassifierImplTest {
    private static final int RECONNECTABLE_REASON = 999999;

    private final MQExceptionClassifierImpl classifier = new MQExceptionClassifierImpl();

    @Test
    void shouldReturnFalseForRuntimeExceptionWithNonReconnectableMQReason() {
        JMSRuntimeException exception = new JMSRuntimeException("runtime error", "ANY", mqException(MQRC_Q_FULL));

        assertFalse(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnTrueForRuntimeExceptionWithReconnectableMQReason() {
        JMSRuntimeException exception = new JMSRuntimeException("runtime error", "JMSCC0005",
                mqException(RECONNECTABLE_REASON));

        assertTrue(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnFalseForRuntimeExceptionWithNonReconnectableErrorCode() {
        JMSRuntimeException exception = new JMSRuntimeException("runtime error", "JMSCC0005");

        assertFalse(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnTrueForRuntimeExceptionWithUnknownErrorCode() {
        JMSRuntimeException exception = new JMSRuntimeException("runtime error");

        assertTrue(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnFalseForCheckedExceptionWithNonReconnectableMQCause() {
        JMSException exception = jmsExceptionWithCause("ANY", mqException(MQRC_Q_FULL));

        assertFalse(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnTrueForCheckedExceptionWithReconnectableMQCause() {
        JMSException exception = jmsExceptionWithCause("JMSCC0005", mqException(RECONNECTABLE_REASON));

        assertTrue(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnFalseForCheckedExceptionWithNonReconnectableLinkedException() {
        JMSException exception = new JMSException("checked error", "ANY");
        exception.setLinkedException(mqException(MQRC_Q_FULL));

        assertFalse(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnFalseForCheckedExceptionWithNonReconnectableErrorCode() {
        JMSException exception = new JMSException("checked error", "JMSCC0005");

        assertFalse(classifier.isReconnectable(exception));
    }

    @Test
    void shouldReturnTrueForCheckedExceptionWithUnknownErrorCode() {
        JMSException exception = new JMSException("checked error");

        assertTrue(classifier.isReconnectable(exception));
    }

    private static JMSException jmsExceptionWithCause(String errorCode, Throwable cause) {
        JMSException exception = new JMSException("checked error", errorCode);
        exception.initCause(cause);
        return exception;
    }

    private static MQException mqException(int reason) {
        return new MQException(MQExceptionClassifierImplTest.class.getSimpleName(), "test", reason, 2);
    }
}



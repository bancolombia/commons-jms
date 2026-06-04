package co.com.bancolombia.commons.jms.mq.config.exceptions;

import co.com.bancolombia.commons.jms.api.exceptions.MQExceptionClassifier;
import com.ibm.mq.MQException;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

import java.util.Map;
import java.util.TreeMap;

import static com.ibm.mq.constants.CMQC.MQRC_ENVIRONMENT_ERROR;
import static com.ibm.mq.constants.CMQC.MQRC_GET_INHIBITED;
import static com.ibm.mq.constants.CMQC.MQRC_MSG_TYPE_ERROR;
import static com.ibm.mq.constants.CMQC.MQRC_OBJECT_IN_USE;
import static com.ibm.mq.constants.CMQC.MQRC_PUT_INHIBITED;
import static com.ibm.mq.constants.CMQC.MQRC_Q_FULL;

public class MQExceptionClassifierImpl implements MQExceptionClassifier {
    private final Map<Integer, Boolean> nonReconnectableReasons;
    private final Map<String, Boolean> nonReconnectableErrorCodes;

    public MQExceptionClassifierImpl() {
        this.nonReconnectableReasons = new TreeMap<>();
        // Errores de configuración y permisos (no se recuperan por reconexión)
        nonReconnectableReasons.put(MQRC_Q_FULL, false);
        nonReconnectableReasons.put(MQRC_GET_INHIBITED, false);
        nonReconnectableReasons.put(MQRC_PUT_INHIBITED, false);
        nonReconnectableReasons.put(MQRC_OBJECT_IN_USE, false);
        nonReconnectableReasons.put(MQRC_MSG_TYPE_ERROR, false);
        nonReconnectableReasons.put(MQRC_ENVIRONMENT_ERROR, false);

        // Códigos no recuperables por reconexión (errores permanentes / de configuración).
        this.nonReconnectableErrorCodes = new TreeMap<>();
        nonReconnectableErrorCodes.put("JMSCC0005", false);
        nonReconnectableErrorCodes.put("JMSCC0006", false);
        nonReconnectableErrorCodes.put("JMSCC0007", false);
        nonReconnectableErrorCodes.put("JMSCC0013", false);
        nonReconnectableErrorCodes.put("JMSCC0014", false);
        nonReconnectableErrorCodes.put("JMSCC0017", false);
        nonReconnectableErrorCodes.put("JMSCC0020", false);
    }

    @Override
    public boolean isReconnectable(JMSRuntimeException e) {
        if (e.getCause() instanceof MQException mqException) {
            return isReconnectable(mqException);
        }
        return isReconnectableByErrorCode(e.getErrorCode());
    }

    @Override
    public boolean isReconnectable(JMSException e) {
        if (e.getCause() instanceof MQException mqException) {
            return isReconnectable(mqException);
        }
        if (e.getLinkedException() instanceof MQException mqException) {
            return isReconnectable(mqException);
        }
        return isReconnectableByErrorCode(e.getErrorCode());
    }

    private boolean isReconnectable(MQException exception) {
        return !nonReconnectableReasons.containsKey(exception.getReason());
    }

    private boolean isReconnectableByErrorCode(String errorCode) {
        return !nonReconnectableErrorCodes.containsKey(errorCode);
    }
}
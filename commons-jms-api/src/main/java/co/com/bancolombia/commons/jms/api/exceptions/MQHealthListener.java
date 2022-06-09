package co.com.bancolombia.commons.jms.api.exceptions;

import javax.jms.JMSException;

public interface MQHealthListener {
    void onInit(String listener);

    void onStarted(String listener);

    void onException(String listener, JMSException exception);
}

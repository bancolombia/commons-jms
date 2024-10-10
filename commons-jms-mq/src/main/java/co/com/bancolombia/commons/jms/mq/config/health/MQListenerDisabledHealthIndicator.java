package co.com.bancolombia.commons.jms.mq.config.health;

import co.com.bancolombia.commons.jms.api.exceptions.MQHealthListener;
import jakarta.jms.JMSException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class MQListenerDisabledHealthIndicator implements MQHealthListener {

    @Override
    public void onInit(String listener) {
        // Do nothing
    }

    @Override
    public void onStarted(String listener) {
        // Do nothing
    }

    @Override
    public void onException(String listener, JMSException exception) {
        // Do nothing
    }

}

package co.com.bancolombia.commons.jms.mq.helper;

import com.ibm.msg.client.jms.JmsReadablePropertyContext;
import lombok.AllArgsConstructor;

import javax.jms.BytesMessage;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.io.Serializable;

@AllArgsConstructor
public class JmsContextImpl implements JMSContext {
    private JmsReadablePropertyContext connection;

    @Override
    public JMSContext createContext(int sessionMode) {
        return null;
    }

    @Override
    public JMSProducer createProducer() {
        return null;
    }

    @Override
    public String getClientID() {
        return null;
    }

    @Override
    public void setClientID(String clientID) {

    }

    @Override
    public ConnectionMetaData getMetaData() {
        return null;
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setAutoStart(boolean autoStart) {

    }

    @Override
    public boolean getAutoStart() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public BytesMessage createBytesMessage() {
        return null;
    }

    @Override
    public MapMessage createMapMessage() {
        return null;
    }

    @Override
    public Message createMessage() {
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return null;
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return null;
    }

    @Override
    public StreamMessage createStreamMessage() {
        return null;
    }

    @Override
    public TextMessage createTextMessage() {
        return null;
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return null;
    }

    @Override
    public boolean getTransacted() {
        return false;
    }

    @Override
    public int getSessionMode() {
        return 0;
    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public void recover() {

    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return null;
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return null;
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return null;
    }

    @Override
    public Queue createQueue(String queueName) {
        return null;
    }

    @Override
    public Topic createTopic(String topicName) {
        return null;
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return null;
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return null;
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return null;
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return null;
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return null;
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return null;
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return null;
    }

    @Override
    public void unsubscribe(String name) {

    }

    @Override
    public void acknowledge() {

    }
}

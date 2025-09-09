# Advanced Settings

## Override Predefined Beans

You can define custom beans to change default behaviors:

- [`MQAutoconfiguration`](https://github.com/bancolombia/commons-jms/blob/main/commons-jms-mq/src/main/java/co/com/bancolombia/commons/jms/mq/config/MQAutoconfiguration.java)
- [`MQAutoconfigurationSender`](https://github.com/bancolombia/commons-jms/blob/main/commons-jms-mq/src/main/java/co/com/bancolombia/commons/jms/mq/config/senders/MQAutoconfigurationSender.java)

You should create and register many fixed response queues for request reply, in this case you can override the
`MQQueueManagerSetter` as following:

```java
    @Bean
    @ConditionalOnMissingBean(MQQueueManagerSetter.class)
    public MQQueueManagerSetter qmSetter(MQProperties properties, MQQueuesContainer container,
                                         @Value("${response.queue.a}") String queueAName,
                                         @Value("${response.queue.b}") String queueBName) {
        return (jmsContext, queue) -> {
            log.info("Self assigning Queue Manager to listening queue: {}", queue.toString());
            MQUtils.setQMNameIfNotSet(jmsContext, queue);
            container.registerQueue(properties.getInputQueue(), queue);
            // Register response queue a with queue manager assigned
            Queue queueA = jmsContext.createQueue(queueAName);
            MQUtils.setQMNameIfNotSet(jmsContext, queueA);
            container.registerQueue(queueAName, queueA);
            // Register response queue b with queue manager assigned
            Queue queueB = jmsContext.createQueue(queueBName);
            MQUtils.setQMNameIfNotSet(jmsContext, queueB);
            container.registerQueue(queueBName, queueB);
        };
    }
```

You can Override any of the next default beans

`MQQueueCustomizer` which sets some predefined properties to `MQQueue`

```java
    public MQQueueCustomizer defaultMQQueueCustomizer() {
        return queue -> {
            if (queue instanceof MQQueue) {
                MQQueue customized = (MQQueue) queue;
                customized.setProperty(WMQ_TARGET_CLIENT, "1");
                customized.setProperty(WMQ_MQMD_READ_ENABLED, "true");
                customized.setProperty(WMQ_MQMD_WRITE_ENABLED, "true");
                customized.setPutAsyncAllowed(WMQ_PUT_ASYNC_ALLOWED_ENABLED);
                customized.setReadAheadAllowed(WMQ_READ_AHEAD_ALLOWED_ENABLED);
            }
        };
    }
```

`MQProducerCustomizer` which sets some predefined properties to `Producer`

```java
    public MQProducerCustomizer defaultMQProducerCustomizer(MQProperties properties) {
        return producer -> {
            if (properties.getProducerTtl() > 0) {
                producer.setTimeToLive(properties.getProducerTtl());
            }
        };
    }
```

`SelectorBuilder`, `CorrelationExtractor` and `MQSchedulerProvider` which works for request reply patterns in fixed queues with selector

```java
    public SelectorBuilder defaultSelectorBuilder() {
        return SelectorBuilder.ofDefaults(); // by JMSCorrelationID
    }

    public CorrelationExtractor defaultCorrelationExtractor() {
        return Message::getJMSCorrelationID;
    }

    public MQSchedulerProvider defaultMqExecutorService() {
        Scheduler scheduler = Schedulers.newBoundedElastic(MAX_THREADS, 2, "selector-pool",
                KEEP_ALIVE_SECONDS);
        return () -> scheduler;
    }
```


## Usual utilities

This class has some utility methods which can help you to do some common tasks

- [`MQUtils`](https://github.com/bancolombia/commons-jms/blob/main/commons-jms-mq/src/main/java/co/com/bancolombia/commons/jms/mq/utils/MQUtils.java)
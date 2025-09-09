# Connection Factory

This library uses the default bean of kind `ConnectionFactory` which is created with default or customized `application.yaml` properties. You also can override it with a custom `ConnectionFactory` bean instantiation.

## Default Properties

Default MQ properties, these are well for local development

```yaml
ibm:
  mq:
    channel: "DEV.APP.SVRCONN"
    user: "app"
    password: passw0rd
    conn-name: "localhost(1414)"
    queue-manager: "QM1"
```

## Custom ConnectionFactory bean

```java
@Bean
public ConnectionFactory myConnectionFactory() {
    JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
    MQConnectionFactory connectionFactory = (MQConnectionFactory) ff.createConnectionFactory();
    // customize here the connection properties in the connectionFactory object
    return connectionFactory;
}
```

# Multiple Broker

If you need multi-broker support you only should define the ConnectionFactory bean with a name
and then use this name on each annotation that you need.

```java
@Bean
public ConnectionFactory domainA() {
    JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
    MQConnectionFactory connectionFactory = (MQConnectionFactory) ff.createConnectionFactory();
    // customize here the connection properties in the connectionFactory object
    return connectionFactory;
}
```

```java
@Bean
public ConnectionFactory domainB() {
    JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.JAKARTA_WMQ_PROVIDER);
    MQConnectionFactory connectionFactory = (MQConnectionFactory) ff.createConnectionFactory();
    // customize here the connection properties in the connectionFactory object
    return connectionFactory;
}

Then in the annotated object you can set the `connectionFactory` property with one of the names `domainA` or `domainB`

This setting is available for:

- `@MQSender`
- `@MQListener`
- `@ReqReply`
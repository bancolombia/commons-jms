# Builder Specification

## A programmatic way to create JMS clients

The library provides a builder specification to create JMS clients programmatically. The main entry point is the
`CommonsJMSSpec` class, which allows you to create senders and listeners with a fluent API.

This approach is useful when you need to create JMS clients dynamically, for example, when you need to create a sender
or listener based on user input or configuration.

### Creating specifications

The specification allows you to create domains, a domain is a group of senders and listeners that share the same
connection factory and configuration.

You should create the connection factories.

```java

```

If you will have listeners, so you need to create the message handlers:

```java

@Bean
public MQMessageHandler messageHandler() {
    return (source, message) -> {
        // Process the message
        return Mono.empty();
    };
}
```

The next step is to create the specification using the builder:

You need to setup at least this properties:

```yaml
commons:
  jms:
    output-concurrency: 1 # set desired concurrency for senders
    reactive: true
    input-concurrency: 1 # set desired concurrency for listeners
```

You also need to annotate your main class with `@EnableMQClientFromSpec` to enable the spec functionality.

```java
@SpringBootApplication(scanBasePackages = "co.com.bancolombia")
@EnableMQClientFromSpec
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class);
    }
}
```

There are two domains, the `domainA` that has two listeners,a sender, a request reply with temporary queue and a request
reply with fixed queue using message selector, and the `domainB` that has one listener and a request reply with 
fixed queue and message selector.

```java

@Bean
public CommonsJMSSpec commonsJMSSpec(MQConfigurationProperties properties, MQMessageHandler handler) {
    return CommonsJMSSpec.builder()
            .withDomain(MQDomainSpec.builder("domainA", connectionFactoryA(properties))
                    .listenQueue("DEV.QUEUE.1", handler)
                    .listenQueue("DEV.QUEUE.2", handler)
                    .withSender()
                    .withTemporaryRequestReply()
                    .withFixedRequestReply()
                    .build())
            .withDomain(MQDomainSpec.builder("domainB", connectionFactoryB(properties))
                    .listenQueue("DEV.QUEUE.3", handler)
                    .withFixedRequestReply()
                    .build())
            .build();
}
```

With this specification the listeners are started automatically when the application starts. It also injects the 
required objects in the MQClient bean, which you can use to send messages and make request-reply calls.

```java
@Component
@AllArgsConstructor
public class MyService {
    private final MQClient mqClient;

    public Mono<Void> sendMessageToDomainA(...) {
        return mqClient.sender("domainA").send(...);
    }

    public Mono<String> requestReplyFromDomainATmp(...) {
        return mqClient.temporaryReqReply("domainA").requestReply(...);
    }

    public Mono<String> requestReplyFromDomainA(...) {
        return mqClient.fixedReqReply("domainA").requestReply(...);
    }

    public Mono<String> requestReplyFromDomainB(String message) {
        return mqClient.fixedReqReply("domainB").requestReply(...);
    }
}
```

Using this approach you prefer to use the specific methods that receives the destination queue and the reply queue 
when fixed request-reply is used.

For `MQMessageSender`:
```java
public interface MQMessageSender {
    // Mono<String> send(String message);

    // Mono<String> send(MQMessageCreator messageCreator);

    Mono<String> send(String destination, String message);

    Mono<String> send(String destination, MQMessageCreator messageCreator);

    Mono<String> send(Destination destination, String message);

    Mono<String> send(Destination destination, MQMessageCreator messageCreator);
}
```
For `MQRequestReply`:
```java
public interface MQRequestReply {
    // Mono<T> requestReply(String message);

    // Mono<T> requestReply(MQMessageCreator messageCreator);

    // Mono<T> requestReply(String message, Duration timeout);

    // Mono<T> requestReply(MQMessageCreator messageCreator, Duration timeout);

    Mono<T> requestReply(String message, Destination request, Duration timeout); // For temporary queues

    Mono<T> requestReply(MQMessageCreator messageCreator, Destination request, Duration timeout); // For temporary queues

    Mono<T> requestReply(String message, Destination request, Destination reply, Duration timeout); // For fixed queues only

    Mono<T> requestReply(MQMessageCreator messageCreator, Destination request, Destination reply, Duration timeout); // For fixed queues only
}
```

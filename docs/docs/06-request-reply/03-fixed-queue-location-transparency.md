# Fixed Queue Location Transparency

This is a more complex implementation of the Request Reply pattern, basically it uses a fixed queue for responses but with a continuous listening mechanism, which allows for more dynamic response handling and increases performance.

This scenario uses an external shared storage (like Redis, Memcached, or a database) to map correlation IDs to specific client instances. This way, when a response is received, any instance can look up the correct client instance to forward the response to the appropriate instance through a HTTP request.

```mermaid
sequenceDiagram
    participant ClientA as Client Instance A
    participant SharedStorage as Redis
    participant RequestQueue as Request Queue
    participant Server
    participant ReplyQueue as Reply Queue
    participant ClientB as Client Instance B

    Note over ClientA: Step 1: Send request message<br>Save (messageId → hostname) in Redis
    ClientA->>RequestQueue: Put request message<br>Save messageId=msg123
    ClientA->>SharedStorage: Set key=msg123 value=hostnameA

    Note over Server: Step 2: Receive and process request
    RequestQueue->>Server: Get request message
    Server-->>Server: Perform processing

    Note over Server: Step 3: Send reply message with correlationId=msg123
    Server->>ReplyQueue: Put reply message<br>CorrelationId=msg123

    Note over ClientB: Step 4: Any client instance receives reply
    ReplyQueue->>ClientB: Get reply message<br>CorrelationId=msg123

    Note over ClientB: Step 5: Lookup hostname in Redis<br>by correlationId=msg123
    ClientB->>SharedStorage: Get key=msg123

    Note over ClientB: Step 6: Forward reply to correct client instance<br>via HTTP request
    ClientB->>ClientA: HTTP POST /handleReply<br>with reply message

    Note over ClientA: Step 7: Process reply in correct client instance
    ClientA-->>ClientA: Resolve original request with reply message
```


For example, you define an interface like the next, so it could be auto implemented by the library:
this [MyRequestReplySingleInstance](https://github.com/bancolombia/commons-jms/blob/main/examples/mq-reactive/src/main/java/co/com/bancolombia/sample/drivenadapters/reqreply/MyRequestReplySingleInstance.java)

To achieve the auto implementation, you should:

1. Annotate the application or a configuration bean with @EnableMQGateway, optionally you can define the base package

  ```java
     @SpringBootApplication(scanBasePackages = "co.com.bancolombia")
     @EnableMQGateway(scanBasePackages = "co.com.bancolombia")
     public class MainApplication {
        public static void main(String[] args) {
            SpringApplication.run(MainApplication.class);
        }
     }
   ```

2. Annotate the interface with @ReqReply, for example

  ```java
    @ReqReply(requestQueue = "DEV.QUEUE.1", replyQueue = "DEV.QUEUE.2", queueType = FIXED_LOCATION_TRANSPARENCY) // in queue names you can use ${some.property.name} spring placeholder notation
    public interface MyRequestReply extends MQRequestReply {
    }
   ```

3. Now you can inject your interface in any spring component.
   [MyRequestReplyAdapter](https://github.com/bancolombia/commons-jms/blob/main/examples/mq-reactive/src/main/java/co/com/bancolombia/sample/drivenadapters/reqreply/MyRequestReplyAdapter.java)

  ```java
    @Component
    @AllArgsConstructor
    public class MyRequestReplyAdapter implements RequestGateway {
        private final MyRequestReply requestReply;
        ...
    }
  ```

  And you can use the next methods to send and receive messages

  ```java
    Mono<T> requestReply(String message);

    Mono<T> requestReply(String message, Duration timeout);

    Mono<T> requestReply(MQMessageCreator messageCreator);

    Mono<T> requestReply(MQMessageCreator messageCreator, Duration timeout);
  ```

Is possible that you require to add the line before the `SpringApplication.run(MainApplication.class, args);` like:

```java
public static void main(String[] args) {
  System.setProperty("spring.devtools.restart.enabled", "false");
  SpringApplication.run(MainApplication.class, args);
}
```

## Additional Configuration

You should import the additional dependency:

```gradle
implementation 'com.github.bancolombia:commons-jms-http-replier:<latest-version-here>'
```

Add annotation to one of your configuration beans or main application class

```java
@EnableMQHttpReplies
@Configuration
public class SomeConfiguration {
    ...
}
```

### Location Manager

You should implement your own `LocationManager` instance:

```java
public interface LocationManager {

    Mono<Void> set(String id, Duration timeout);

    Mono<String> get(String id);
}
```

For example [RedisCache](https://github.com/bancolombia/commons-jms/blob/main/examples/adapter-redis/src/main/java/co/com/bancolombia/sample/redis/RedisCache.java)

### HTTP Server

You also need to import the default http server configuration, to achieve this you can define the next configuration properties:

```yaml
commons.jms.reply.port: 8000 # Port should be different from the exposed by the application
commons.jms.reply.timeout: 5000 # milliseconds
```

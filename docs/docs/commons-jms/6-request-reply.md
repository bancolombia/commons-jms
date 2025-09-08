---
sidebar_position: 6
---

# Request Reply

### Request Reply Temporary Queue

This is a basic implementation of the Request Reply pattern, basically it creates a temporary queue for responses and
starts listening it, it creates its listener and autogenerate an instance that can be pseudo defined by the user as an
interface, which implements the interface.

The application that attends the request should follow the replyTo header which is automatically injected through the
operation:

```java
textMessage.setJMSReplyTo(temporaryQueue)
```

This approach is only implemented for reactive projects, so you can define your own interface with at least one of the
next interface signatures:

```java
    Mono<Message> requestReply(String message);

    Mono<Message> requestReply(String message, Duration timeout);

    Mono<Message> requestReply(MQMessageCreator messageCreator);

    Mono<Message> requestReply(MQMessageCreator messageCreator, Duration timeout);
```

For example, you define an interface like the next, so it could be auto implemented by the library:
this [MyRequestReplyTmp](https://github.com/bancolombia/commons-jms/blob/main/examples/mq-reactive/src/main/java/co/com/bancolombia/sample/drivenadapters/reqreply/MyRequestReplyTmp.java)

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
    @ReqReply(requestQueue = "DEV.QUEUE.1") // in queue names you can use ${some.property.name} spring placeholder notation
    public interface MyRequestReplyTmp extends MQRequestReply {
    }
   ```

3. Now you can inject your interface in any spring component.
   [MyRequestReplyAdapter](https://github.com/bancolombia/commons-jms/blob/main/examples/mq-reactive/src/main/java/co/com/bancolombia/sample/drivenadapters/reqreply/MyRequestReplyAdapter.java)

  ```java
    @Component
    @AllArgsConstructor
    public class MyRequestReplyAdapter implements RequestGateway {
        private final MyRequestReplyTmp requestReply;
        ...
    }
  ```

Is possible that you require to add the line before the `SpringApplication.run(MainApplication.class, args);` like:

```java
 public static void main(String[] args) {
    System.setProperty("spring.devtools.restart.enabled", "false");
    SpringApplication.run(MainApplication.class, args);
}
```

### Request Reply Fixed Queue

When the use of a temporary queue is not available for persistent reasons, or lost of messages is not allowed
you can use a Request Reply pattern based on a fixed queue, you should consider the next scenarios:

- Single Queue Manager:
  In this scenario you should not consider any setup. Following code snippet can show a basic implementation:

    ```java
    @ReqReply(requestQueue = "DEV.QUEUE.1", replyQueue = "DEV.QUEUE.2", queueType = FIXED)
    public interface MyRequestReply extends MQRequestReply {
    }
   ```
  Then inject this interface to your adapter like with temporary queue

- Multiple Queue Manager or Clustering:
  In this scenario you should guarantee that:
    - the application that attends the request follow the replyTo header.
    - set to `true` the property `commons.jms.input-queue-set-queue-manager` to identify and set the queue manager to
      the
      response queue (this guarantees that the application that attends the request send the response to the specific
      queue manager).
    - Then the same like with a single Queue Manager
# Customizing ReplyTo

## Custom Identifiers

You can see this section in [Customizing Messages](/commons-jms/docs/customizing-messages)

## Custom ReplyTo

By default, when using the Request Reply pattern with temporary queues, the `replyTo` header is automatically set to the temporary queue created for the response, it also works with fixed queues so you don't have to worry about it. However, there may be scenarios where you need to set a custom `replyTo` destination.

In fixed queue scenarios, you can set a custom `replyTo` destination by implementing a custom `MQMessageCreator` when sending the request message. This allows you to specify the desired `replyTo` destination that aligns with your application's requirements.

```java
public Mono<Result> doRequest(String request) {
  return requestReplyTmp.requestReply(context -> {
              Message message = context.createTextMessage(request);
              message.setJMSReplyTo(destination); // instance of Destination class
              return message;
          }, Duration.ofSeconds(5))
          .map(...);
  }
```

The library has a container map of queues, so you can extract queue by name, so you need to inject the bean of type `MQQueuesContainer` and use the method `get(queueName)` to get the destination.

In general for fixed queues the base code of the `MQMessageCreator` is and usually yo don't need to implement a custom one:

```java
return ctx -> {
  Message jmsMessage = ctx.createTextMessage(message);
  Queue queue = queuesContainer.get(replyQueue);
  message.setJMSReplyTo(queue);
  if (log.isInfoEnabled() && queue != null) {
      log.info("Setting queue for reply to: {}", queue.getQueueName());
  }
  return jmsMessage;
};
```
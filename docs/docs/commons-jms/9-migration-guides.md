---
sidebar_position: 9
---

# Migration

## From 1.x.x to 2.x.x

Change notes:

- `@MQListener` has removed support to listen a temporary queue, because `@ReqReply` use this behaviour by default.
- `@ReqReply` has added support to do a request reply pattern using fixed queues with get message by selector.

Actions:

- `@EnableMQSelectorMessageListener` has been removed, now you can use `@ReqReply` directly using `queueType` attribute
  with value `FIXED`.
- `@EnableMQMessageSender` has been removed, now you should use `@EnableMQGateway`.
- `@EnableReqReply` has been removed, now you should use `@EnableMQGateway` passing the same `scanBasePackages`
  property.
- property `replyQueueTemp` has been renamed to `replyQueue` in `@ReqReply`.
- `commons.jms.input-queue-alias` has been removed now you only can set the alias with `replyQueue`.
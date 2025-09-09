# Configuration Properties

All this properties can be set in `application.yaml` file

## General properties

- `commons.jms.reactive`: Should be set to `true` for reactive (Reactor) projects.

## Listener properties

There are three configuratuion properties:

- `commons.jms.input-concurrency`: *Number of open connections to listening the queue*, applies for fixed and temporary queues.
- `commons.jms.input-queue`: *Name of the listening queue*, use only when listen for a fixed queue
- `commons.jms.input-queue-set-queue-manager`: *Boolean* to enable to set the resolved queue manager when needed.

## Sender properties

There are three configuration properties:

- `commons.jms.output-concurrency`: *Number of open connections to send messages to a queue*.
- `commons.jms.output-queue`: *Name of the default queue to send messages*.
- `commons.jms.producer-ttl`: *Long value in milliseconds which sets the time to live of a message put onto a queue. A
  value of 0 means live indefinitely*.

## Connection Retry properties

- `commons.jms.max-retries`: Number of retries when the connection is lost.
- `commons.jms.initial-retry-interval-millis`: Initial interval between retries in milliseconds.
- `commons.jms.retry-multiplier`: Multiplier for the interval between retries.

For more information about the connection retry properties, please refer
to [Resilience4j Retry](https://resilience4j.readme.io/docs/retry)
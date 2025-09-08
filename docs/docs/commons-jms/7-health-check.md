---
sidebar_position: 7
---

# Health Checks

Commons JMS has two health indicators, the first one is the default spring boot jms health indicator, which checks the connection. The second one is the `MQHealthIndicator` which checks the listeners connection to the queue manager.

Both health indicators are enabled by default, but you can disable them by setting the next properties:

- application.properties
  ```properties
  management.health.jms.enabled=false
  ```
- application.yaml
  ```yaml
  management:
    health:
      jms:
        enabled: false
  ```
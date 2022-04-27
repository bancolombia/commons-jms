# MQ Sample

To run this sample you need to run a MQ Server.

You can do it with docker, please refer to [docker hub](https://hub.docker.com/r/ibmcom/mq) for more details:

```shell
docker run -e LICENSE=accept -e MQ_QMGR_NAME=QM1 -p 1414:1414 -p 9443:9443 -d --name ibmmq -e MQ_APP_PASSWORD=passw0rd ibmcom/mq
```

When MQ Server is running, run
this [MainApplication](src/main/java/co/com/bancolombia/jms/replier/app/MainApplication.java)

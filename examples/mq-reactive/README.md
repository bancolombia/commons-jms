# MQ Sample

To run this sample you need to run a MQ Server.

You can do it with docker, please refer to [docker hub](https://hub.docker.com/r/ibmcom/mq) for more details:

```shell
docker run -e LICENSE=accept -e MQ_QMGR_NAME=QM1 -p 1414:1414 -p 9443:9443 -d --name ibmmq -e MQ_APP_PASSWORD=passw0rd ibmcom/mq
```

When MQ Server is running, run
this [MainApplication](src/main/java/co/com/bancolombia/jms/sample/app/MainApplication.java)

You should start the mq-reactive-replier app module too

Visit this [http://localhost:8080/api/mq](http://localhost:8080/api/mq) to send and listen a message.
Visit this [http://localhost:8080/api/mq](http://localhost:8080/api/mq/reqreply) to make a request reply.

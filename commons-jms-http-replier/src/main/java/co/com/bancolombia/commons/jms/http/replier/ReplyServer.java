package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Log4j2
@UtilityClass
public class ReplyServer {

    public static WebServer startServer(HttpReactiveReplyRouter router, int port, boolean start) {
        log.info("Starting Reply Server for mq messages in port {}", port);
        ReactiveWebServerFactory factory = new NettyReactiveWebServerFactory(port);
        WebServer server = factory.getWebServer(RouterFunctions.toHttpHandler(httpRouter(router)));
        if (start) {
            server.start();
            log.info("Reply Server started in port {}", port);
        }
        return server;
    }


    public static RouterFunction<ServerResponse> httpRouter(HttpReactiveReplyRouter router) {
        return RouterFunctions.route()
                .POST("/reply", request -> replyHandler(router, request))
                .build();
    }

    public static Mono<ServerResponse> replyHandler(HttpReactiveReplyRouter router, ServerRequest request) {
        return request.bodyToMono(JmsMessage.class)
                .flatMap(message -> Mono.fromRunnable(() -> router.reply(message.getCorrelationID(), message)))
                .then(ServerResponse.noContent().build());
    }
}

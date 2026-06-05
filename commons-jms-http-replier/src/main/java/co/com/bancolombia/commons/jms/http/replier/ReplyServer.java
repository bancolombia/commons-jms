package co.com.bancolombia.commons.jms.http.replier;

import co.com.bancolombia.commons.jms.api.model.JmsMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@Log4j2
@UtilityClass
public class ReplyServer {

    public static DisposableServer startServer(HttpReactiveReplyRouter router, int port, boolean start) {
        log.info("Starting Reply Server for mq messages in port {}", port);
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(
                RouterFunctions.toHttpHandler(httpRouter(router))
        );
        HttpServer server = HttpServer.create()
                .port(port)
                .handle(adapter);

        if (start) {
            DisposableServer disposableServer = server.bindNow();
            log.info("Reply Server started in port {}", port);
            return disposableServer;
        }
        return null;
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

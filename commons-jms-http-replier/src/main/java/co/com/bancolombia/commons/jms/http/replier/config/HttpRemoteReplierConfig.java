package co.com.bancolombia.commons.jms.http.replier.config;

import co.com.bancolombia.commons.jms.http.replier.HttpReactiveReplyRouter;
import co.com.bancolombia.commons.jms.http.replier.ReplyClient;
import co.com.bancolombia.commons.jms.http.replier.ReplyServer;
import co.com.bancolombia.commons.jms.http.replier.api.LocationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpRemoteReplierConfig {

    @Bean
    public boolean defaultHttpReplyServer(HttpReactiveReplyRouter router, @Value("${commons.jms.reply.port}") int port) {
        ReplyServer.startServer(router, port, true);
        return true;
    }

    @Bean
    public HttpReactiveReplyRouter defaultHttpReactiveReplyRouter(ReplyClient client, LocationManager manager) {
        return new HttpReactiveReplyRouter(client, manager);
    }

    @Bean
    public ReplyClient defaultHttpReplyClient(WebClient.Builder builder, @Value("${commons.jms.reply.timeout}") int timeout) {
        return new ReplyClient(builder, timeout);
    }

}

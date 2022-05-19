package cz.cacek.test;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.nike.riposte.server.Server;
import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import io.netty.channel.ChannelHandlerContext;

/**
 * The App!
 */
public class App {

    public static void main(String[] args) throws Exception {
        Server server = new Server(new AppServerConfig());
        server.startup();
    }

    public static class AppServerConfig implements ServerConfig {
        private final Collection<Endpoint<?>> endpoints = Collections.singleton(new HelloWorldEndpoint());

        @Override
        public Collection<Endpoint<?>> appEndpoints() {
            return endpoints;
        }

//        @Override
//        public boolean isEndpointsUseSsl() {
//            return true;
//        }
    }

    public static class HelloWorldEndpoint extends StandardEndpoint<Void, String> {
        @Override
        public Matcher requestMatcher() {
            return Matcher.match("/hello");
        }

        @Override
        public CompletableFuture<ResponseInfo<String>> execute(RequestInfo<Void> request,
                                                               Executor longRunningTaskExecutor,
                                                               ChannelHandlerContext ctx) {
            return CompletableFuture.completedFuture(
                ResponseInfo.newBuilder("Hello, world!")
                            .withDesiredContentWriterMimeType("text/plain")
                            .build()
            );
        }
    }
}

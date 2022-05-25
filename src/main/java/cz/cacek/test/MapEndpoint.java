package cz.cacek.test;

import static io.netty.handler.codec.http.HttpMethod.DELETE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;

import java.security.AccessControlException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.validation.constraints.NotNull;

import com.hazelcast.client.impl.protocol.AuthenticationStatus;
import com.hazelcast.client.impl.protocol.exception.ErrorHolder;
import com.hazelcast.core.HazelcastInstance;
import com.nike.riposte.server.error.exception.Forbidden403Exception;
import com.nike.riposte.server.error.exception.Unauthorized401Exception;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.server.http.impl.FullResponseInfo;
import com.nike.riposte.util.Matcher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;

public class MapEndpoint extends StandardEndpoint<String, String> {

    private final HazelcastInstance hz;

    public MapEndpoint(HazelcastInstance hz) {
        this.hz = hz;
    }

    @Override
    public Matcher requestMatcher() {
        return Matcher.match("/map/{mapname}/{key}");
    }

    @Override
    public CompletableFuture<ResponseInfo<String>> execute(RequestInfo<String> request, Executor longRunningTaskExecutor,
            ChannelHandlerContext ctx) {
        return CompletableFuture.supplyAsync(() -> process(request, ctx), longRunningTaskExecutor);
    }

    private @NotNull FullResponseInfo<String> process(RequestInfo<String> request, ChannelHandlerContext ctx) {
        String username = null;
        String password = null;

        String authorizationHeader = request.getHeaders().get("Authorization");
        String path = request.getPath();
        if (authorizationHeader != null) {
            final String[] authSplit = authorizationHeader.split(" ");
            if (authSplit.length != 2 || !"Basic".equals(authSplit[0])) {
                throw new Unauthorized401Exception("Authorization header does not contain Basic", path, authorizationHeader);
            }
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedBytes;
            try {
                decodedBytes = decoder.decode(authSplit[1]);
            } catch (IllegalArgumentException ex) {
                throw new Unauthorized401Exception(
                        "Malformed Authorization header (not Base64 encoded), caused by: " + ex.toString(), path,
                        authorizationHeader);
            }

            String pair = new String(decodedBytes);
            String[] userDetails = pair.split(":", 2);
            if (userDetails.length != 2) {
                throw new Unauthorized401Exception("Malformed Authorization header.", path, authorizationHeader);
            }
            username = userDetails[0];
            password = userDetails[1];
        }

        LocalClient lc = new LocalClient(hz, ctx.channel().localAddress(), ctx.channel().remoteAddress(), username, password);
        // we have to authenticate even when username and password are null
        AuthenticationStatus authnStatus = lc.authenticate(username, password);
        if (authnStatus != AuthenticationStatus.AUTHENTICATED) {
            throw new Unauthorized401Exception("Authentication failed", path, authorizationHeader);
        }

        String mapname = request.getPathParam("mapname");
        String key = request.getPathParam("key");

        ResponseInfo.newBuilder();
        String returnVal = "";
        HttpMethod httpmethod = request.getMethod();
        try {
            if (POST.equals(httpmethod) || PUT.equals(httpmethod)) {
                returnVal = lc.mapPut(mapname, key, request.getContent());
            } else if (GET.equals(httpmethod)) {
                returnVal = lc.mapGet(mapname, key);
            } else if (DELETE.equals(httpmethod)) {
                returnVal = lc.mapDelete(mapname, key);
            }
        } catch (ClientCallFailedException e) {
            ErrorHolder errorHolder = e.getErr().get(0);
            if (AccessControlException.class.getName().equals(errorHolder.getClassName())) {
                throw new Forbidden403Exception("Forbidden", path, authorizationHeader);
            } else {
                throw e;
            }
        }
        return ResponseInfo.newBuilder(returnVal).withDesiredContentWriterMimeType("text/plain").build();
    }
}
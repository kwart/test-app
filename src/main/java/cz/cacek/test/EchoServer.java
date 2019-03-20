/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package cz.cacek.test;

import static io.netty.handler.ssl.SslProvider.OPENSSL;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * This is a reproducer for IBM 8 + BoringSSL sigAlg issue. It's based on example EchoServer from netty sources:
 * <a href="https://github.com/netty/netty/tree/netty-4.1.34.Final/example/src/main/java/io/netty/example/echo">
 * /example/src/main/java/io/netty/example/echo</a>
 * <p>
 * Run it with IBM Java 8 SDK.
 */
public final class EchoServer {

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final String CERT= "-----BEGIN CERTIFICATE-----\n" + 
            "MIICUzCCAbygAwIBAgIEUiROSjANBgkqhkiG9w0BAQUFADBuMQswCQYDVQQGEwJU\n" + 
            "UjERMA8GA1UECBMISXN0YW5idWwxETAPBgNVBAcTCElzdGFuYnVsMRIwEAYDVQQK\n" + 
            "EwlIYXplbGNhc3QxDDAKBgNVBAsMA1ImRDEXMBUGA1UEAxMOSGF6ZWxjYXN0LCBJ\n" + 
            "bmMwHhcNMTMwOTAyMDgzNzMwWhcNMjMwNzEyMDgzNzMwWjBuMQswCQYDVQQGEwJU\n" + 
            "UjERMA8GA1UECBMISXN0YW5idWwxETAPBgNVBAcTCElzdGFuYnVsMRIwEAYDVQQK\n" + 
            "EwlIYXplbGNhc3QxDDAKBgNVBAsMA1ImRDEXMBUGA1UEAxMOSGF6ZWxjYXN0LCBJ\n" + 
            "bmMwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAJVr0Aj6B6nFef8hdLflwwLB\n" + 
            "271XPuI9+KMZwPnEwivPqRtbs3Bj9IiyzM5RavZxqrUHrnoTgeMLZhw08GS1WG+K\n" + 
            "BTlTkjHJ5NHbBwImBfvLzXTQo/KfmvWph+AlJm4j0iPAKWfrwiI2vnLuiNhCs0GZ\n" + 
            "KYOZaysosqScKWRWLYM/AgMBAAEwDQYJKoZIhvcNAQEFBQADgYEAEvQo6VdQri4S\n" + 
            "EtjgktSDTS2WcRXKE6RvESoOp7OfOenI6KeS0K81KCJ7ha+ILjl6iwj4Jkwm3Si1\n" + 
            "XGFx/wcILQkjXm6tBl4EC6YrpJq1EXR6MyP6GcDk1mQ9QfA/Vf6ocsAiBxUX4WDl\n" + 
            "0yfw/PKQ1CfbCkIFYhFaM2gyAeDdkF8=\n" + 
            "-----END CERTIFICATE-----\n";
    static final String PRIVKEY = "-----BEGIN PRIVATE KEY-----\n" + 
            "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJVr0Aj6B6nFef8h\n" + 
            "dLflwwLB271XPuI9+KMZwPnEwivPqRtbs3Bj9IiyzM5RavZxqrUHrnoTgeMLZhw0\n" + 
            "8GS1WG+KBTlTkjHJ5NHbBwImBfvLzXTQo/KfmvWph+AlJm4j0iPAKWfrwiI2vnLu\n" + 
            "iNhCs0GZKYOZaysosqScKWRWLYM/AgMBAAECgYBxRu7MH2E337IBLUfjMpiIupbw\n" + 
            "D/hoZDrey2N09ymNNT0qtHZwuhZkm8iQkUDZ3Iph+5TWj3tkuPuMXsXwU1ra6SKl\n" + 
            "ltPHuoPEwa51h1tQuKVkALdkSjD/8B9pw8IBBZzCL2CIQUq3YUpqFsmroS9YON86\n" + 
            "oHmHPJxDTXHEUjWmgQJBAOA0zP496Lua41g87P2e9L234jjTe5GjhUODStJG6Fo6\n" + 
            "O3sNlz8R1M646G4OpSU/+arcScEkssOFV7Vo95vLci8CQQCqnCQ7JawjWuXOTzVX\n" + 
            "eNBVwYADJ9jsrKIqn1tKHW6KhE7YtI9E0Xs+VqVEtXZaAi9slWKSfPcd3jgjVx4p\n" + 
            "cQvxAkA4Fb051C7Fz0cTqZn3D65VTwxt/qkok2kgrFUpKMey2mJKs+mjw0gitiqe\n" + 
            "bVdubAR+c0CX6iA1vMNmA+38sPXNAkAi2uV4A/lH+9EJNhtytGbzriWF/4UMzTQQ\n" + 
            "OyA+YwbrW550HWdLqvRfxvnasKvuNZYUu+w4ezlNK9ISPkEWPFcxAkEAkZQv3GqX\n" + 
            "894qPnfh3F/yaaoKoHQ1pmjNJ2g1z8RdlEK2u+nXbmfFt9er6h4/xo0iw0OcTgFl\n" + 
            "h8LfM8vZAagTsw==\n" + 
            "-----END PRIVATE KEY-----\n";

    public static void main(String[] args) throws Exception {
        if (! System.getProperty("java.vendor").startsWith("IBM")
                || ! System.getProperty("java.version").startsWith("1.8")) {
            System.err.println("Run this reproducer with IBM Java 8.");
            System.exit(13);
        }
        System.setProperty("javax.net.debug", "all");

        // Configure SSL.
        ByteArrayInputStream certIs = new ByteArrayInputStream(CERT.getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream pkIs = new ByteArrayInputStream(PRIVKEY.getBytes(StandardCharsets.UTF_8));
        final SslContext sslCtx = SslContextBuilder.forServer(certIs, pkIs).sslProvider(OPENSSL).build();
        certIs.close();
        pkIs.close();

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final EchoServerHandler serverHandler = new EchoServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline()
                             .addLast(sslCtx.newHandler(ch.alloc()))
//                             .addLast(new LoggingHandler(LogLevel.INFO))
                             .addLast(serverHandler);
                 }
             });

            // Start the server.
            ChannelFuture f = b.bind(PORT).sync();

            EchoClient.clientMain();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    @Sharable
    public static class EchoServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }

    /**
     * Sends one message when a connection is open and echoes back any received
     * data to the server.  Simply put, the echo client initiates the ping-pong
     * traffic between the echo client and server by sending the first message to
     * the server.
     */
    public static final class EchoClient {

        static final String HOST = System.getProperty("host", "127.0.0.1");
        static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

        public static void clientMain() throws Exception {
            // Configure SSL.git
            final SslContext sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .sslProvider(SslProvider.JDK)
                    .build();

            // Configure the client.
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group)
                 .channel(NioSocketChannel.class)
                 .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     public void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(sslCtx.newHandler(ch.alloc(), HOST, PORT))
//                                        .addLast(new LoggingHandler(LogLevel.INFO))
                                        .addLast(new EchoClientHandler());
                            }
                 });

                // Start the client.
                ChannelFuture f = b.connect(HOST, PORT).sync();

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
            } finally {
                // Shut down the event loop to terminate all threads.
                group.shutdownGracefully();
            }
        }
    }

    /**
     * Handler implementation for the echo client.  It initiates the ping-pong
     * traffic between the echo client and server by sending the first message to
     * the server.
     */
    public static class EchoClientHandler extends ChannelInboundHandlerAdapter {

        private final ByteBuf firstMessage;

        /**
         * Creates a client-side handler.
         */
        public EchoClientHandler() {
            firstMessage = Unpooled.buffer(EchoClient.SIZE);
            for (int i = 0; i < firstMessage.capacity(); i ++) {
                firstMessage.writeByte((byte) i);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(firstMessage);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ctx.write(msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
           ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }

}

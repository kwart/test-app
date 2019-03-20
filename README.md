# Reproducer: netty + BoringSSL + IBM Java TLS handshake issue

Issue report: netty/netty-tcnative#448

## Steps to reproduce
```
$ mvn -version
Apache Maven 3.5.4 (1edded0938998edf8bf061f1ceb3cfdeccf443fe; 2018-06-17T20:33:14+02:00)
Maven home: /home/josef/tools/maven
Java version: 1.8.0_151, vendor: IBM Corporation, runtime: /home/josef/java/ibm-java-x86_64-80/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.15.0-46-generic", arch: "amd64", family: "unix"

$ mvn clean install
...

$ java -jar target/netty-reproducer-boringssl-ibm8.jar
...
ssl: Ignoring alias key: signature does not conform to negotiated signature algorithms
ssl: Ignoring alias key: signature does not conform to negotiated signature algorithms
ssl: Ignoring alias key: signature does not conform to negotiated signature algorithms
ssl: Ignoring alias key: signature does not conform to negotiated signature algorithms
ssl: Ignoring alias key: signature does not conform to negotiated signature algorithms
ssl: Ignoring alias key: signature does not conform to negotiated signature algorithms
[Raw read]: length = 5
0000: 15 03 03 00 02                                     .....

io.netty.handler.codec.DecoderException: javax.net.ssl.SSLHandshakeException: error:100000b8:SSL routines:OPENSSL_internal:NO_SHARED_CIPHER
    at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:472)[Raw read]: length = 2

    at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:278)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:359)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:345)
    at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:337)
    at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1408)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:359)
0000: 02 28                                              ..

    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:345)nioEventLoopGroup-4-1, READ: TLSv1.2 Alert, length = 2

    at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:930)
    at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:163)
nioEventLoopGroup-4-1   at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:677)
, RECV TLSv1.2 ALERT:  fatal,   at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:612)
    at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:529)
    at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:491)
    at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:905)
    at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
    at java.lang.Thread.run(Thread.java:811)
Caused by: javax.net.ssl.SSLHandshakeException: error:100000b8:SSL routines:OPENSSL_internal:NO_SHARED_CIPHER
    at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.sslReadErrorResult(ReferenceCountedOpenSslEngine.java:1224)
    at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.unwrap(ReferenceCountedOpenSslEngine.java:1185)
    at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.unwrap(ReferenceCountedOpenSslEngine.java:1256)
    at io.netty.handler.ssl.ReferenceCountedOpenSslEngine.unwrap(ReferenceCountedOpenSslEngine.java:1299)
    at io.netty.handler.ssl.SslHandler$SslEngineType$1.unwrap(SslHandler.java:217)
    at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1330)
    at io.netty.handler.ssl.SslHandler.decodeNonJdkCompatible(SslHandler.java:1237)
    at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1274)
    at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:502)
    at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:441)
    ... 16 more
handshake_failure
nioEventLoopGroup-4-1, fatal: engine already closed.  Rethrowing javax.net.ssl.SSLException: Received fatal alert: handshake_failure
nioEventLoopGroup-4-1, fatal: engine already closed.  Rethrowing javax.net.ssl.SSLException: Received fatal alert: handshake_failure
nioEventLoopGroup-4-1, called closeOutbound()
nioEventLoopGroup-4-1, closeOutboundInternal()
nioEventLoopGroup-4-1, SEND TLSv1.2 ALERT:  warning, description = close_notify
nioEventLoopGroup-4-1, WRITE: TLSv1.2 Alert, length = 2
nioEventLoopGroup-4-1, called closeInbound()
nioEventLoopGroup-4-1, closeInboundInternal()
nioEventLoopGroup-4-1, closeOutboundInternal()
io.netty.handler.codec.DecoderException: javax.net.ssl.SSLException: Received fatal alert: handshake_failure
    at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:472)
    at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:278)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:359)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:345)
    at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:337)
    at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1408)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:359)
    at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:345)
    at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:930)
    at io.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:163)
    at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:677)
    at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:612)
    at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:529)
    at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:491)
    at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:905)
    at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
    at java.lang.Thread.run(Thread.java:811)
Caused by: javax.net.ssl.SSLException: Received fatal alert: handshake_failure
    at com.ibm.jsse2.k.a(k.java:19)
    at com.ibm.jsse2.aq.a(aq.java:604)
    at com.ibm.jsse2.aq.a(aq.java:192)
    at com.ibm.jsse2.aq.j(aq.java:161)
    at com.ibm.jsse2.aq.b(aq.java:303)
    at com.ibm.jsse2.aq.a(aq.java:316)
    at com.ibm.jsse2.aq.unwrap(aq.java:370)
    at javax.net.ssl.SSLEngine.unwrap(SSLEngine.java:11)
    at io.netty.handler.ssl.SslHandler$SslEngineType$3.unwrap(SslHandler.java:295)
    at io.netty.handler.ssl.SslHandler.unwrap(SslHandler.java:1330)
    at io.netty.handler.ssl.SslHandler.decodeJdkCompatible(SslHandler.java:1225)
    at io.netty.handler.ssl.SslHandler.decode(SslHandler.java:1272)
    at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:502)
    at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:441)
    ... 16 more
nioEventLoopGroup-4-1, called closeOutbound()
nioEventLoopGroup-4-1, closeOutboundInternal()
[Raw write]: length = 7
0000: 15 03 03 00 02 01 00                               .......

nioEventLoopGroup-4-1, called closeOutbound()
nioEventLoopGroup-4-1, closeOutboundInternal()

```
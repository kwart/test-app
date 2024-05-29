# Netty-tcnative reproducer - leaking OpenSslEngine instances

**Issue link:** https://github.com/netty/netty/issues/14085

**Description:** Instances of `io.netty.handler.ssl.OpenSslEngine` class are not released properly when `SslProvider.OPENSSL` is used.

The reproducer does several TLS handshakes in a row, prints a help and waits.

```
******* Explicit release is NOT enabled
******* Running TLS handshakes. Count: 3
>>> Handshake starting
>>> Handshake finished
>>> Handshake starting
>>> Handshake finished
>>> Handshake starting
>>> Handshake finished
******* Waiting for debug/heapdump/etc
Try:
    jcmd 52844 GC.run
    jcmd 52844 GC.class_histogram | grep OpenSslEngine
```

There shouldn't be any `OpenSslEngine` instance, but there are all of them used for handshakes.

```bash
$ jcmd 52844 GC.run
52844:
Command executed successfully

$ jcmd 52844 GC.class_histogram | grep OpenSslEngine
 151:             6            768  io.netty.handler.ssl.OpenSslEngine
 202:             6            432  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$DefaultOpenSslSession
 302:             6            192  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$2
 331:             6            144  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$1
 411:             6             96  io.netty.handler.ssl.ReferenceCountedOpenSslContext$DefaultOpenSslEngineMap
 412:             4             96  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$HandshakeState
 618:             1             32  [Lio.netty.handler.ssl.ReferenceCountedOpenSslEngine$HandshakeState;
```
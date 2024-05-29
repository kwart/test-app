# Netty-tcnative reproducer - leaking OpenSslEngine instances

**Issue link:** https://github.com/netty/netty/issues/14085

**Description:** Instances of `io.netty.handler.ssl.OpenSslEngine` class are not released properly when `SslProvider.OPENSSL` is used.

The reproducer creates several `OpenSslEngine` instances, prints the help and waits.

```
******* Explicit release is NOT enabled
******* Creating OpenSslEngine instances: 10
******* Waiting for debug/heapdump/etc
Try:
    jcmd 47192 GC.run
    jcmd 47192 GC.class_histogram | grep OpenSslEngine
```

There shouldn't be any `OpenSslEngine` instance, but there are all of the created ones.

```bash
$ jcmd 47192 GC.run
47192:
Command executed successfully

$ jcmd 47192 GC.class_histogram | grep OpenSslEngine
 142:            10           1280  io.netty.handler.ssl.OpenSslEngine
 182:            10            720  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$DefaultOpenSslSession
 262:            10            320  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$2
 294:            10            240  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$1
 339:            10            160  io.netty.handler.ssl.ReferenceCountedOpenSslContext$DefaultOpenSslEngineMap
 408:             4             96  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$HandshakeState
 577:             1             32  [Lio.netty.handler.ssl.ReferenceCountedOpenSslEngine$HandshakeState;
```
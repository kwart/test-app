# Netty-tcnative reproducer - leaking OpenSslEngine instances

Issue link: https://github.com/netty/netty/issues/xxx

Description: Instances of `io.netty.handler.ssl.OpenSslEngine` class are not released properly even when `SslProvider.OPENSSL` is used.

The reproducer emulates several TLS handshakes, executes `System.gc()` and then just waits.

After the handshakes finish, you can check objects on the heap:

```bash
jmap -histo <application PID> | grep io.netty.handler.ssl
```

There shouldn't be any `OpenSslEngine` instance, but there are all of them used for handshakes.

```bash
$ jmap -histo 42927 | grep io.netty.handler.ssl |head -n 10
  64:            20           2560  io.netty.handler.ssl.OpenSslEngine
  96:            20           1440  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$DefaultOpenSslSession
 107:            20           1280  io.netty.handler.ssl.OpenSslSessionCache$1
 141:            20            800  io.netty.handler.ssl.DefaultOpenSslKeyMaterial
 142:            10            800  io.netty.handler.ssl.OpenSslClientContext
 143:            10            800  io.netty.handler.ssl.OpenSslServerContext
 144:            20            800  io.netty.handler.ssl.util.LazyX509Certificate
 161:            20            640  io.netty.handler.ssl.ReferenceCountedOpenSslEngine$2
 183:            20            480  io.netty.handler.ssl.OpenSslX509KeyManagerFactory$OpenSslKeyManagerFactorySpi$ProviderFactory$OpenSslPopulatedKeyMaterialProvider
 184:            20            480  io.netty.handler.ssl.ReferenceCountedOpenSslContext$1
```
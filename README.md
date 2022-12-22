# Netty-tcnative reproducer - TLSv1.3 handshake issue

OpenSSL / TLS 1.3 handshake fails to complete when mutual authentication is enabled and the default `TrustManagerFactory` is used.

With this configuration client's `SSLEngine` reports `BUFFER_OVERFLOW`for the `unwrap()` operation and
the value of `SSLSession.getApllicationBufferSize()` got increased.

Even if we try to increase the size of the application buffer, there is subsequent issue with `BUFFER_UNDERFLOW` for `wrap()` operation.



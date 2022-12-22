# Netty-tcnative reproducer - TLSv1.3 handshake issue (netty/netty#13073)

Issue link: https://github.com/netty/netty/issues/13073

OpenSSL / TLS 1.3 handshake fails to complete when mutual authentication is enabled and the default `TrustManagerFactory` is used.

With this configuration client's `SSLEngine` reports `BUFFER_OVERFLOW`for the `unwrap()` operation and
the value of `SSLSession.getApllicationBufferSize()` got increased.

Even if we try to increase the size of the application buffer, there is subsequent issue with `BUFFER_UNDERFLOW` for `wrap()` operation.

## Handshake progress - sample output from the reproducer

### TLSv1.3 without resizing application buffer

```
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_WRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_TASK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: BUFFER_OVERFLOW
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_OVERFLOW
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:54:25 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_OVERFLOW
...
```

### TLSv1.3 with resizing application buffer

After the application buffer resize it wrongly expects more application data on client's input.

```
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_WRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_TASK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: BUFFER_OVERFLOW
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_OVERFLOW
2022-12-22-12:16:57 [WARNING] cz.cacek.test.TLSHandshaker handle Unexpected BUFFER_OVERFLOW after the unwrap
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle SSLEngine session buffer sizes - application: 33344, packet: 16389
2022-12-22-12:16:57 [WARNING] cz.cacek.test.TLSHandshaker handle Resizing the appBuffer, the session size had changed!
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle src has remaining bytes: 16256
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-12:16:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
```

### TLSv1.2 output

Correct output with properly finishing TLS handshake:

```
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_WRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_TASK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: BUFFER_OVERFLOW
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:57 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_TASK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_TASK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_WRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_TASK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NEED_WRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Wrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NOT_HANDSHAKING
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: OK
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NEED_UNWRAP
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Unwrap result status: BUFFER_UNDERFLOW
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Client handshake=NOT_HANDSHAKING
2022-12-22-11:53:58 [INFO] cz.cacek.test.TLSHandshaker handle Server handshake=NOT_HANDSHAKING
```
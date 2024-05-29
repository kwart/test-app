package cz.cacek.test;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

public class App {

    private static final int ENGINE_COUNT = 10;
    private static final boolean USE_EXPLICIT_RELEASE = false;

    public static void main(String[] args) throws Exception {
        System.out.println("******* Explicit release is " + (USE_EXPLICIT_RELEASE ? "" : "NOT") + " enabled");
        System.out.println("******* Creating OpenSslEngine instances: " + ENGINE_COUNT);
        for (int i = 0; i < ENGINE_COUNT; i++) {
            SslContextBuilder builder = SslContextBuilder.forClient();
            builder.sslProvider(SslProvider.OPENSSL);
            SslContext context = builder.build();
            SSLEngine engine = context.newEngine(UnpooledByteBufAllocator.DEFAULT);
            engine.beginHandshake();
            if (USE_EXPLICIT_RELEASE) {
                releaseIfNeeded((ReferenceCounted) engine);
            }
        }
        System.out.println("******* Waiting for debug/heapdump/etc");
        long pid = ProcessHandle.current().pid();
        System.out.println("Try:");
        System.out.println("\tjcmd " + pid + " GC.run");
        System.out.println("\tjcmd " + pid + " GC.class_histogram | grep OpenSslEngine");
        TimeUnit.MINUTES.sleep(60);
    }

    private static void releaseIfNeeded(ReferenceCounted counted) {
        if (counted.refCnt() > 0) {
            ReferenceCountUtil.safeRelease(counted);
        }
    }
}

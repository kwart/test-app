package cz.cacek.test;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;

import javax.net.ssl.SSLException;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.nike.riposte.server.Server;
import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.server.http.Endpoint;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class MapServer implements Runnable {

    private final HazelcastInstance hz;

    public MapServer() {
        hz = Hazelcast.newHazelcastInstance();
        hz.getMap("test").put("key", "value");
    }

    public static void main(String[] args) throws Exception {
        new MapServer().run();
    }

    public void run() {
        Server server = new Server(new MapServerConfig());
        try {
            server.startup();
        } catch (CertificateException | IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    class MapServerConfig implements ServerConfig {
        private final Collection<Endpoint<?>> endpoints = Collections.singleton(new MapEndpoint(hz));

        private final String tlsCertChainFile = System.getProperty("mapserver.certchain", "/opt/hazelcast-chain.crt");
        private final String tlsKeyFile = System.getProperty("mapserver.certchain", "/opt/hazelcast.key");
        private final boolean tlsEnabled = !Boolean.getBoolean("mapserver.notls");

        @Override
        public Collection<Endpoint<?>> appEndpoints() {
            return endpoints;
        }

        @Override
        public boolean isEndpointsUseSsl() {
            return tlsEnabled;
        }

        public SslContext createSslContext() throws SSLException, CertificateException {
            return SslContextBuilder.forServer(new File(tlsCertChainFile), new File(tlsKeyFile)).build();
        }

    }

}

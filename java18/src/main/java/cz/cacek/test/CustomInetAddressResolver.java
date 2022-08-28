package cz.cacek.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.Arrays;
import java.util.stream.Stream;

public class CustomInetAddressResolver implements InetAddressResolver {
    private static final byte[] LOOPBACK_IP = new byte[] { 127, 0, 0, 1 };

    @Override
    public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException {
        if ("very.local.host".equals(host)) {
            return Stream.of(InetAddress.getByAddress(LOOPBACK_IP));
        }
        throw new UnknownHostException("Sorry jako");
    }

    @Override
    public String lookupByAddress(byte[] addr) throws UnknownHostException {
        if (Arrays.equals(LOOPBACK_IP, addr)) {
            return "very.local.host";
        } else {
            throw new UnknownHostException("Sorry jako");
        }
    }
}

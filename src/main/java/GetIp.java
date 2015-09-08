
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Helper program which prints an IP address. If no address is found, it prints
 * "0.0.0.0".
 * 
 * @author Josef Cacek
 */
public class GetIp {

	public static void main(String[] args) throws SocketException {
		String mode = args.length > 0 ? args[0] : "auto";

		if ("--help".equals(mode) || "-h".equals(mode)) {
			System.out.println("GetIp - Simple util to retrieve an IP address");
			System.out.println();
			System.out.println("Usage:");
			System.out.println("\tjava <address|networkInterface|'auto'>");
			System.out.println();
		}

		InetAddress result = null;
		NetworkInterface loopback = null;
		if ("auto".equals(mode)) {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				try {
					if (netint.isUp()) {
						if (netint.isLoopback()) {
							loopback = netint;
						} else if (!netint.isPointToPoint()) {
							result = getIpv4ForNetworkInterface(netint);
							if (result != null)
								break;
						}
					}
				} catch (Exception e) {
					// let's ignore errors
				}
			}
			// if result==null use loopback
			if (result == null) {
				result = getIpv4ForNetworkInterface(loopback);
			}
		} else {
			NetworkInterface netint = null;
			try {
				netint = NetworkInterface.getByName(mode);
			} catch (Exception e) {
				try {
					netint = NetworkInterface.getByInetAddress(InetAddress.getByName(mode));
				} catch (Exception ex) {
					// let's ignore errors
				}
			}
			result = getIpv4ForNetworkInterface(netint);
		}
		System.out.println((result instanceof Inet4Address) ? result.getHostAddress() : "0.0.0.0");
	}

	private static InetAddress getIpv4ForNetworkInterface(NetworkInterface netint) {
		if (netint != null) {
			for (InetAddress addr : Collections.list(netint.getInetAddresses())) {
				if (addr instanceof Inet4Address) {
					return addr;
				}
			}
		}
		return null;
	}

}

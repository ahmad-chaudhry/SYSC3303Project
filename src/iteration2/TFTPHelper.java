package iteration2;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class TFTPHelper {
	String name;
	boolean verbose;

	public TFTPHelper(String name, boolean verbose) {
		this.name = name;
		this.verbose = verbose;
	}

	public void sendPacket(Packet sendPacket, DatagramSocket socket, InetAddress addr, int port) throws IOException {
		byte[] SendBytes = sendPacket.convertBytes();
		DatagramPacket sndPacket = new DatagramPacket(SendBytes, SendBytes.length, addr, port);
		
		if (verbose) {
			// Sending packet 
			System.out.println(name+": Sending packet: \n");
			System.out.println("To Host: " + sndPacket.getAddress()); // same host address (local computer)
			System.out.println("Destination Host port: " + sndPacket.getPort()); // Port 23
			int len = sndPacket.getLength(); // calculate packet length
			System.out.println("Length: " + len);
			System.out.print("Containing: ");

			System.out.println(new String(sndPacket.getData(), 2, len - 2));
			System.out.println(Arrays.toString(SendBytes));

			try {
				socket.send(sndPacket); // send packet on socket
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println(name+": Packet sent\n");

		}

	}
	
	public void receive(Packet sendPacket, DatagramSocket socket, InetAddress addr, int port) throws IOException {
		byte[] SendBytes = sendPacket.convertBytes();
		DatagramPacket sndPacket = new DatagramPacket(SendBytes, SendBytes.length, addr, port);
		
		if (verbose) {
			// Sending packet 
			System.out.println(name+": Sending packet: \n");
			System.out.println("To Host: " + sndPacket.getAddress()); // same host address (local computer)
			System.out.println("Destination Host port: " + sndPacket.getPort()); // Port 23
			int len = sndPacket.getLength(); // calculate packet length
			System.out.println("Length: " + len);
			System.out.print("Containing: ");

			System.out.println(new String(sndPacket.getData(), 2, len - 2));
			System.out.println(Arrays.toString(SendBytes));

			try {
				socket.send(sndPacket); // send packet on socket
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println(name+": Packet sent\n");

		}

	}
	
	public Packet receivePacket(DatagramSocket socket) {
		Packet received = new Packet();
		try {
			byte[] bytesReceived = new byte[Packet.PACKETSIZE];
			DatagramPacket packet = new DatagramPacket(bytesReceived, bytesReceived.length);
			socket.receive(packet);
			received.SetAddr(packet.getAddress());
			received.setPort(packet.getPort());
			
			//NEED TO EXTRACT DATA FROM bRec in Packet.java class using a method
			//GOOGLE HOW TO EXTRACT VALUES FROM BYTE ARRAY TO SET VALUES FOR PACKET
			
			return received;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return received;
	}

}

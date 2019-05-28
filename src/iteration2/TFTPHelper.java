package iteration2;

/**
 * TFTPHelper.java
 * 
 * Iteration 2:
 * @author Ahmad Chaudhry
 * 
 * 
 * 
 */
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class TFTPHelper {
	String name;
	boolean verbose;

	/**
	 * The default constructor
	 * 
	 * @param name name of class using it
	 * @param verbose if verbose is used
	 */
	public TFTPHelper(String name, boolean verbose) {
		this.name = name;
		this.verbose = verbose;
	}

	/**
	 * Method used to send a packet
	 * 
	 * @param sendPacket the packet that needs to be send
	 * @param socket the socket to use
	 * @param addr the address to use
	 * @param port the port number to use
	 * @throws IOException
	 */
	public void sendPacket(Packet sendPacket, DatagramSocket socket, InetAddress addr, int port) throws IOException {
		byte[] SendBytes = sendPacket.convertBytes();
		DatagramPacket sndPacket = new DatagramPacket(SendBytes, SendBytes.length, addr, port);

		if (verbose) {
			// Sending packet
			System.out.println(name + ": Sending packet: \n");
			System.out.println("To Host: " + sndPacket.getAddress()); // same host address (local computer)
			System.out.println("Destination Host port: " + sndPacket.getPort()); // Port 23
			int len = sndPacket.getLength(); // calculate packet length
			System.out.println("Length: " + len);
			System.out.print("Containing: ");

			System.out.println(new String(sndPacket.getData(), 2, len - 2));
			System.out.println(Arrays.toString(SendBytes));
		}
		try {
			socket.send(sndPacket); // send packet on socket
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(name + ": Packet sent\n");

	}

	/**
	 * Method used to receive packets
	 * 
	 * @param socket the socket to wait at
	 * @return
	 */
	public Packet receivePacket(DatagramSocket socket) {
		//create empty packet to store info
		Packet received = new Packet();
		try {
			//create holder byte array of size Packets
			byte[] bytesReceived = new byte[Packet.PACKETSIZE];
			//create datagrampacket
			DatagramPacket packet = new DatagramPacket(bytesReceived, bytesReceived.length);
			//receive info fro socket
			socket.receive(packet);
			//set the address and port of the packet to the one received
			received.setAddr(packet.getAddress());
			received.setPort(packet.getPort());
			// uses method in Packet.java to sort bytes for packet data
			if (received.receiveBytes(packet.getData()) == true) {
				if (verbose) {
					// Sending packet
					System.out.println(name + ": Receving packet: \n");
					System.out.println(name + ": of type " + received.packetType());
					System.out.println("To Host: " + packet.getAddress()); // same host address (local computer)
					System.out.println("Destination Host port: " + packet.getPort()); // Port 23
					int len = packet.getLength(); // calculate packet length
					System.out.println("Length: " + len);
					System.out.print("Containing: ");
					System.out.println(Arrays.toString(bytesReceived));
				}
				return received;
			} else {
				System.out.println(name + " Failed to receive packet");
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return received;
	}

	/**
	 * Method used to write data to a file
	 * 
	 * @param fOut the file
	 * @param block the block
	 */
	public void WriteData(FileOutputStream fOut, byte[] block) {
		int i = 0;
		byte[] write = null;
		for (i = 0; i < block.length; i++) {
			if (block[i] == 0) {
				write = new byte[i];
				for (int j = 0; j < i; j++) {
					write[j] = block[j];
				}
				break;
			}
		}
		if (i >= block.length)
			write = block;
		try {
			fOut.write(write);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method used to read data from file at a position
	 * 
	 * @param fIn the file
	 * @param block the block #
	 * @param size the size
	 * @return
	 */
	public byte[] ReadData(FileInputStream fIn, int block, int size) {
		try {
			long fileSize = fIn.getChannel().size();
			byte[] out = new byte[size];
			fIn.read(out);
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Method needed to create input file else a null error is directed in
	// TFTPClient under operation 2
	/**
	 * Method used to create a input file 
	 * 
	 * @param path the path to file
	 * @return
	 */
	public FileInputStream OpenInputFile(String path) {
		try {
			FileInputStream in = new FileInputStream(path);
			return in;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}

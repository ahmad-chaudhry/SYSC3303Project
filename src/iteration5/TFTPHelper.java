package iteration5;

/**
 * TFTPHelper.java
 * 
 * Iteration 4:
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
	int timeout = 10000;
	int retries = 5;

	/**
	 * The default constructor
	 * 
	 * @param name    name of class using it
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
	 * @param socket     the socket to use
	 * @param addr       the address to use
	 * @param port       the port number to use
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
	 * Method used to send a packet
	 * 
	 * @param sendPacket the packet that needs to be send
	 * @param socket     the socket to use
	 * @param addr       the address to use
	 * @param port       the port number to use
	 * @throws IOException
	 */
	public void sendPacket(byte[] sendPacket, DatagramSocket socket, InetAddress addr, int port) throws IOException {
		DatagramPacket sndPacket = new DatagramPacket(sendPacket, sendPacket.length, addr, port);
		if (verbose) {
			// Sending packet
			System.out.println(name + ": Sending packet: \n");
			System.out.println("To Host: " + sndPacket.getAddress()); // same host address (local computer)
			System.out.println("Destination Host port: " + sndPacket.getPort()); // Port 23
			int len = sndPacket.getLength(); // calculate packet length
			System.out.println("Length: " + len);
			System.out.print("Containing: ");

			System.out.println(new String(sndPacket.getData(), 2, len - 2));
			System.out.println(Arrays.toString(sendPacket));
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
		// create empty packet to store info
		Packet received = new Packet();
		try {
			// create holder byte array of size Packets
			byte[] bytesReceived = new byte[Packet.PACKETSIZE];
			// create datagrampacket
			DatagramPacket packet = new DatagramPacket(bytesReceived, bytesReceived.length);
			// receive info fro socket
			socket.receive(packet);
			// set the address and port of the packet to the one received
			received.SetAddr(packet.getAddress());
			received.setPort(packet.getPort());
			
			byte[] rBytes = new byte[packet.getLength()];
			System.arraycopy(bytesReceived, 0, rBytes, 0, packet.getLength());
			// uses method in Packet.java to sort bytes for packet data
			if (received.receiveBytes(rBytes) == true) {
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
	 * Method used to receive packets with timeout
	 * 
	 * @param socket the socket to wait at
	 * @return
	 * @throws IOException
	 */
	public Packet receivePacket(DatagramSocket socket, int timeout) throws IOException {
		// create empty packet to store info
		Packet received = new Packet();
		socket.setSoTimeout(timeout);
		// create holder byte array of size Packets
		byte[] bytesReceived = new byte[Packet.PACKETSIZE];
		// create datagrampacket
		DatagramPacket packet = new DatagramPacket(bytesReceived, bytesReceived.length);
		// receive info fro socket
		socket.receive(packet);
		// set the address and port of the packet to the one received
		received.SetAddr(packet.getAddress());
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

		return received;
	}

	/**
	 * Method used to write data to a file
	 * 
	 * @param fOut  the file
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
	 * Method used to write data to a file
	 * 
	 * @param fOut  the file
	 * @param block the block
	 */
	public void WriteData(BufferedOutputStream fOut, byte[] block) {
		try {
			fOut.write(block);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used to read data from file at a position
	 * 
	 * @param fIn   the file
	 * @param block the block #
	 * @param size  the size
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

	/**
	 * Method used to read data from file at a position
	 * 
	 * @param fIn   the file
	 * @param block the block #
	 * @param size  the size
	 * @return
	 */
	public byte[] ReadData(BufferedInputStream fIn, int block, int size) {
		try {
			byte[] out = new byte[size];
			int read = fIn.read(out);
			if (read != size && read > 0) {
				byte[] res = new byte[read];
				System.arraycopy(out, 0, res, 0, read);
				out = res;
			}
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
			return null;
		}
	}

	/**
	 * Method used to create a output file
	 * 
	 * @param path the path to file
	 * @return
	 */
	
	public BufferedOutputStream OpenOFile(String path) {
		File dir = new File(path);
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dir, true));
			return out;
		} catch (FileNotFoundException e) {
			// The code should just quit if it somehow comes to this.
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * Method used to check that packet is valid
	 * 
	 * @param P the packet 
	 * @param Inquiry the inquiry to check against
	 * @return
	 */
	public boolean validPacket(Packet P, int Inquiry) {
		if (P.GetInquiry() == 5) {
			System.out.println("Error received.");
			return true;
		} else if (P.GetInquiry() != Inquiry && Inquiry != 0) {
			System.out.println("Unexpected request.");
			return false;
		} else if (!P.getValid()) {
			System.out.println("Invalid packet received.");
			return false;
		}
		return true;
	}
	
	/**
	 * Method used to get bytes to a string 
	 * 
	 * @param arr the array of bytes
	 * @return
	 */
	public String byteToString(byte[] arr) {
		String res = "";
		int i = 0;
		int j = 0;
		for (i = 0; i < arr.length; i++) {
			res += " ";
			if (arr[i] >= 0)
				res += " ";
			if (Math.abs(arr[i]) < 10)
				res += "  ";
			else if (Math.abs(arr[i]) < 100)
				res += " ";
			if (i != arr.length - 1)
				res += arr[i] + ",";
			else
				res += arr[i] + ".";
			j++;
			if (j == 16 && i != arr.length - 1) {
				j = 0;
				res += "\n";
			}
		}
		return res;
	}

}

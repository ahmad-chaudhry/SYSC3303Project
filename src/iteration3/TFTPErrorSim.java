package iteration3;

/**
 * TFTPErrorSim.java 
 * 
 * Iteration 2
 * 
 * @author ahmad chaudhry
 * 
 */

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class TFTPErrorSim {

	private DatagramSocket receiveSocket, sendSocket, sendReceiveSocket, changeSocket;
	private TFTPHelper helper;
	private Packet Packet;
	private int ClientPort, ServerPort;
	private boolean verbose;
	private boolean filetransfer;
	private int operation = -1;
	private int requestPacketType = 0; // 1=RRQ, 2=WRQ
	private int errorPacketType = 0; // 1=DATA, 2=ACK
	private int packetBlockNumber = 0;
	private int clientrequest = -1;
	private InetAddress address;

	public TFTPErrorSim(boolean verbose) {
		filetransfer = false;
		this.verbose = verbose;
		helper = new TFTPHelper("Sim", verbose);
		try {
			address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			// receive socket binded to port 23
			receiveSocket = new DatagramSocket(23);
			// send and receive socket
			sendReceiveSocket = new DatagramSocket();
			// send socket
			sendSocket = new DatagramSocket();

		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void OPset(Scanner sc) {
		System.out.println("What type of operation would you like to perform ");
		System.out.println("0 - Normal operation");
		System.out.println("1 - Error Code 4: Change RRQ to WRQ or WRQ to RRQ");
		System.out.println("2 - Error Code 4: Change RRQ/WRQ to an invalid request");
		System.out.println("3 - Error Code 4: Remove Filename");
		System.out.println("4 - Error Code 5: Change Port Number");
		System.out.println("5 - Lost a packet");
		System.out.println("6 - Delay a packet");
		System.out.println("7 - Duplicate a Packet");
		while (true) {
			String input = sc.nextLine();
			if (input.equals("0")) {
				operation = 0;
				break;
			} else if (input.equals("1")) {
				operation = 1;
				break;
			} else if (input.equals("2")) {
				operation = 2;
				break;
			} else if (input.equals("3")) {
				operation = 3;
				break;
			} else if (input.equals("4")) {
				operation = 4;
				break;
			} else if (input.equals("5")) {
				operation = 5;
				break;
			} else if (input.equals("6")) {
				operation = 6;
				break;
			} else if (input.equals("7")) {
				operation = 7;
				break;
			} else {
				System.out.println();
				System.out.println("Invalid choice, What type of operation would you like to perform ");
				System.out.println("Normal operation (0)");
				System.out.println("1 - Error Code 4: Change RRQ to WRQ or WRQ to RRQ");
				System.out.println("2 - Error Code 4: Change RRQ/WRQ to an invalid request");
				System.out.println("3 - Error Code 4: Remove Filename");
				System.out.println("4 - Error Code 5: Change Port Number");
				System.out.println("5 - Lost a packet");
				System.out.println("6 - Delay a packet");
				System.out.println("7 - Duplicate a Packet");
			}
		}
		if (operation == 5 || operation == 6 || operation == 7) {
			getPacketType(sc);
		} else {
			start();
		}
	}

	public void getPacketType(Scanner sc) {
		if (operation == 5 || operation == 6 || operation == 7) {
			System.out.println("What type of request would you like to lose/delay/duplicate?");
			System.out.println("RRQ (R)");
			System.out.println("WRQ (W)");
			while (true) {
				String input = sc.nextLine();
				if (input.toUpperCase().equals("R")) {
					// RRQ packet
					requestPacketType = 1;
					break;
				} else if (input.toUpperCase().equals("W")) {
					// WRQ packet
					requestPacketType = 2;
					break;
				} else {
					System.out.println("Invalid response, choose again");
					System.out.println("What type of request would you like to lose/delay/duplicate?");
					System.out.println("RRQ (R)");
					System.out.println("WRQ (W)");
				}
			}
		}
		getPacketNumber(sc);
	}

	public void getPacketNumber(Scanner sc) {
		if (requestPacketType == 1 || requestPacketType == 2) {
			System.out.println("What type of packet would you like to lose/delay/duplicate?");
			System.out.println("DATA (D)");
			System.out.println("ACK (A)");
			while (true) {
				String input = sc.nextLine();
				if (input.toUpperCase().equals("D")) {
					// RRQ packet
					errorPacketType = 3;
					break;
				} else if (input.toUpperCase().equals("A")) {
					// WRQ packet
					errorPacketType = 4;
					break;
				} else {
					System.out.println("Invalid response, choose again");
					System.out.println("What type of packet would you like to lose/delay/duplicate?");
					System.out.println("DATA (D)");
					System.out.println("ACK (A)");
				}
			}
			System.out.println("What block number would you like to lose/delay/duplicate (enter a number)?");
			while (true) {
				String blockNum = sc.nextLine();
				try {
					Integer integer = Integer.parseInt(blockNum);
					// save the entered block number
					packetBlockNumber = integer;
					break;
				} catch (NumberFormatException e) {
					System.out.println("Invalid response, enter a valid number");
					System.out.println("What block number would you like to lose/delay/duplicate?");
				}
			}
		}
		start();
	}

	public void start() {
		// Infinite for loop so it comes back here to wait for Client
		for (;;) {
			byte[] data = new byte[Packet.PACKETSIZE];

			// Receive Packet from Client
			if (!filetransfer) {
				Packet = helper.receivePacket(receiveSocket);
			} else {
				try {
					Packet = helper.receivePacket(sendSocket, 500);
				} catch (IOException e) {
					System.out.println("No response from client, assuming client completed.");
					filetransfer = false;
					receiveSocket.close();
					sendSocket.close();
					sendReceiveSocket.close();
					return;
				}
			}

			// Extracting the Packet Received from the Client
			ClientPort = Packet.GetPort();
			if (clientrequest == -1) {
				clientrequest = Packet.GetInquiry();
			}

			data = Packet.GetData();
			int len = data.length;

			String received = new String(data, 0, len);

			// server sees them as being sent by the client
			if (!filetransfer && operation < 4 && operation > -1) {
				putError(Packet, operation, sendReceiveSocket, 69);
			} else if (filetransfer && operation == 4 && Packet.GetPacketNum() == 0) {
				Packet p = Packet;
				putError(Packet, operation, sendReceiveSocket, ServerPort);
				try {
					helper.sendPacket(p, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (!filetransfer) {
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, 69);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (filetransfer && operation >= 5 && operation <= 7) {
				putError(Packet, operation, sendReceiveSocket, ServerPort);
			} else {
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			try {
				Packet = helper.receivePacket(sendReceiveSocket, 2500);
			} catch (IOException e) {
				System.out.println("No response from server, assuming server completed.");
				filetransfer = false;
				receiveSocket.close();
				sendReceiveSocket.close();
				return;
			}

			ServerPort = Packet.GetPort();

			// Extract information received from server
			System.out.println("Packet Received From Port: " + ServerPort);
			System.out.println("Packet Received From Address: " + Packet.GetAddress() + "\n");

			// Data received
			data = Packet.GetData();

			// Form a String from the byte array, and print the string.
			received = new String(data, 0, data.length);
			System.out.println("Packet Received in String: " + received + "\n");

			// Send Packet received to client
			System.out.println("Send Packet received from Server to the Client");

			// client sees them as being sent by the server
			if (filetransfer && operation == 4 && Packet.GetPacketNum() == 0) {
				Packet p = Packet;
				putError(Packet, operation, sendSocket, ClientPort);
				operation = -1;
				try {
					helper.sendPacket(p, sendSocket, address, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (filetransfer && operation >= 5 && operation <= 7) {
				putError(Packet, operation, sendSocket, ClientPort);
			} else {
				try {
					helper.sendPacket(Packet, sendSocket, address, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (!filetransfer) {
				filetransfer = true;
			}
		}
	}

	// Simulate the User error and send the new packet to the Server
	public void putError(Packet newPacket, int userInput, DatagramSocket soc, int port) {

		String sender = "";
		Packet p1;

		// Data received
		int blockNumber = newPacket.GetPacketNum();
		int packetType = newPacket.GetInquiry();
		byte[] data = newPacket.GetData();
		byte[] mode = newPacket.GetMode().getBytes();
		

		// Putting the packet data into a String
		String msg = new String(data);

		System.out.println("Received Packet Length: " + msg.length() + "\n");
		//used for filename case
		byte[] newData;

		switch (userInput) {
		case 0:

			System.out.println("Sending unchanged Request to Server to establish a connection");
			System.out.println("Packet Received in Bytes: " + msg);

			// forward the packet received
			try {
				helper.sendPacket(newPacket, soc, address, port);
			} catch (IOException e8) {
				e8.printStackTrace();
			}

			this.operation = -1;

			break;

		case 1: // Change opcode RRQ to WRQ or WRQ to RRQ

			System.out.println("Changing TFTP opcode");
			System.out.println("Original Packet: " + msg);

			// Change RRQ to WRQ or WRQ to RRQ
			if (data[1] == 1) {
				data[1] = 2;
				System.out.println("changed from RRQ to WRQ");
			} else if (data[1] == 2) {
				data[1] = 1;
				System.out.println("changed from WRQ to RRQ");
			}

			// New Packet
			System.out.println("Modified Packet: " + helper.byteToString(data) + "\n");

			// Send Packet to Server
			Packet = new Packet(data);
			try {
				helper.sendPacket(Packet, soc, address, port);
			} catch (IOException e7) {
				e7.printStackTrace();
			}

			this.operation = -1;

			break;

		case 2: // change from RRQ/WRQ to an invalid request

			System.out.println("Changing request opcode to an invalid opcode");
			System.out.println("Original Packet: " + msg);

			// change opcode to an invalid opcode
			data[1] = 9;
			System.out.println("Modified Packet: " + helper.byteToString(data) + "\n");

			// send new data to server
			try {
				helper.sendPacket(data, soc, address, port);
			} catch (IOException e6) {
				e6.printStackTrace();
			}

			this.operation = -1;

			break;

		case 3: // Replace the Filename

			System.out.println("Removing filename");

			// remove filename
			newData = new byte[3 + mode.length];
			newData[0] = data[0];
			newData[1] = data[1];
			newData[2] = 0;
			System.arraycopy(mode, 0, newData, 3, mode.length);
			newData[2 + mode.length] = 0;

			// New Packet
			System.out.println("Modified Packet: " + helper.byteToString(newData) + "\n");

			// Send Packet to Server
			Packet = new Packet(newData);
			try {
				helper.sendPacket(Packet, soc, address, port);
			} catch (IOException e5) {
				e5.printStackTrace();
			}

			this.operation = -1;

			break;

		case 4: // Change the Socket Invalid TID and send to the server.
			System.out.println("Changing the Port");
			try {
				changeSocket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}

			if (port == ClientPort) {
				System.out.println("Send to client through an invalid TID");
			} else {
				System.out.println("Send to server through an invalid TID");
			}

			try {
				helper.sendPacket(Packet, changeSocket, address, port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			// Receive Packet from server
			Packet = helper.receivePacket(changeSocket);

			System.out.println("ERROR Packet received");
			// Extract info
			System.out.println("Packet Received From Port: " + Packet.GetPort());
			System.out.println("Packet Received From Address: " + Packet.GetAddress() + "\n");

			System.out.println("Error Code : " + Packet.getErrorCode());
			System.out.println("Error Message: " + Packet.getErrorMssg());

			break;

		case 5: // Lose a packet

			System.out.println("Beginning losing a packet error simulation.");

			if (requestPacketType != clientrequest) {
				System.out.println("Wrong request.");
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				userInput = -1;
				break;
			}

			if (!(errorPacketType == packetType && packetBlockNumber == blockNumber)) {
				System.out.println("Wrong packet type or block number");
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}

			p1 = helper.receivePacket(soc);

			if (port == ClientPort) { // sending to the client
				soc = sendReceiveSocket;
				sender = "Server";
			} else { // sending to the server
				soc = sendSocket;
				sender = "Client";
			}

			// sender times out
			System.out.println("receiving Message from " + sender + " time out");
			Packet = helper.receivePacket(soc);

			if (port == ClientPort) {
				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, address, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			userInput = -1;

			break;

		case 6: // Delaying a Packet

			System.out.println("Beginning delaying a packet error simulation.");

			if (requestPacketType != clientrequest) {
				System.out.println("Wrong request.");
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				userInput = -1;
				break;
			}

			if (!(errorPacketType == packetType && packetBlockNumber == blockNumber)) {
				System.out.println("Wrong packet type or block number");
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}

			// we have the correct packet number and type and request.

			p1 = helper.receivePacket(soc);

			if (port == ClientPort) { // sending to the client
				soc = sendReceiveSocket;
				sender = "Server";
			} else { // sending to the server
				soc = sendSocket;
				sender = "Client";
			}

			Packet = helper.receivePacket(soc);

			if (port == ClientPort) {
				try {
					helper.sendPacket(Packet, sendSocket, address, ClientPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					helper.sendPacket(p1, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, ServerPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					helper.sendPacket(p1, sendSocket, address, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			userInput = -1;
			break;

		case 7: // Duplicating packets

			System.out.println("Beginning duplicating a packet error simulation.");

			if (requestPacketType != clientrequest) {
				System.out.println("Wrong request.");
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				userInput = -1;
				break;
			}

			if (!(errorPacketType == packetType && packetBlockNumber == blockNumber)) {
				System.out.println("Wrong packet type or block number");
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}

			if (port == ClientPort) { // sending to the client
				sender = "Client";
			} else { // sending to the server
				sender = "Server";
			}
			p1 = Packet;

			System.out.println("Forwarding packet to " + sender);
			try {
				helper.sendPacket(p1, soc, address, port);
			} catch (IOException e3) {
				e3.printStackTrace();
			}

			System.out.println("Receiving Packet from " + sender);
			Packet = helper.receivePacket(soc);

			System.out.println("Sending duplicate packet " + sender);
			try {
				helper.sendPacket(p1, soc, address, port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			if (port == ClientPort && ((requestPacketType == 1 && errorPacketType == 3)
					|| (requestPacketType == 2 && errorPacketType == 4))) { // sending to the client
				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, ServerPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				System.out.println("Receiving packet from server");
				Packet = helper.receivePacket(sendReceiveSocket);

				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, address, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (port == ClientPort) {
				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (port == ServerPort && ((requestPacketType == 1 && errorPacketType == 4)
					|| (requestPacketType == 2 && errorPacketType == 3))) { // sending to the
				// server
				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, address, ClientPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				System.out.println("Receiving packet from client");
				Packet = helper.receivePacket(sendSocket);

				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, address, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			userInput = -1;
			break;

		}
	}// End of Method

	public static void main(String args[]) {
		// ask user if they want verbose
		boolean verbose;
		boolean running = true;
		Scanner sc = new Scanner(System.in);
		System.out.println("This is the ERRORSIM");
		System.out.println();
		while (true) {
			System.out.println("Would you like to run it in verbose mode (Y/N)?");
			String input = sc.nextLine();
			if (input.toUpperCase().equals("Y")) {
				verbose = true;
				break;
			}
			if (input.toUpperCase().equals("N")) {
				verbose = false;
				break;
			}
			System.out.println("Mode not valid, please choose either \"Y\" or \"N\" for verbose");
		}
		TFTPErrorSim TFTPErrorSim = new TFTPErrorSim(verbose);
		while (running) {
			TFTPErrorSim.OPset(sc);

			while (true) {
				System.out.println("Run again? (Y/N)?");
				// check users input
				String input = sc.nextLine();
				// if they want to continue keep running
				if (input.toUpperCase().equals("Y")) {
					running = true;
					break;
				}
				// if they want to stop set running false it will break from while loop
				if (input.toUpperCase().equals("N")) {
					running = false;
					break;
				}
				System.out.println("Invalid response please choose \"Y\" or \"N\": ");
			}
		}
		System.out.println();
		System.out.println("TFTPErrorSim has been shutdown");
		sc.close();
	}
}

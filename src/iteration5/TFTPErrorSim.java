package iteration5;

/**
 * TFTPErrorSim.java 
 * 
 * Iteration 4
 * 
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
	private InetAddress address, clientAddress, serverAddress;
	private int oldACK, newACK;

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

	/**
	 * 
	 * Method used to set the operation desired on the error sim
	 * 
	 */
	public void OPset(Scanner sc) {
		System.out.println("What type of operation would you like to perform ");
		System.out.println("0 - Normal operation");
		System.out.println("1 - Error Code 4: Change RRQ to WRQ or WRQ to RRQ");
		System.out.println("2 - Error Code 4: Change RRQ/WRQ to an invalid request");
		System.out.println("3 - Error Code 4: Remove Filename");
		System.out.println("4 - Error Code 4: DATA packet size greater than 512bytes");
		System.out.println("5 - Error Code 5: Change Port Number");
		System.out.println("6 - Change ACK block Number");
		System.out.println("7 - Lost a packet");
		System.out.println("8 - Delay a packet");
		System.out.println("9 - Duplicate a Packet");
		// waits for a valid entry
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
			} else if (input.equals("8")) {
				operation = 8;
				break;
			} else if (input.equals("9")) {
				operation = 9;
				break;
			} else {
				System.out.println();
				System.out.println("Invalid choice, What type of operation would you like to perform ");
				System.out.println("0 - Normal operation");
				System.out.println("1 - Error Code 4: Change RRQ to WRQ or WRQ to RRQ");
				System.out.println("2 - Error Code 4: Change RRQ/WRQ to an invalid request");
				System.out.println("3 - Error Code 4: Remove Filename");
				System.out.println("4 - Error Code 4: DATA packet size greater than 512bytes");
				System.out.println("5 - Error Code 5: Change Port Number");
				System.out.println("6 - Change ACK block Number");
				System.out.println("7 - Lost a packet");
				System.out.println("8 - Delay a packet");
				System.out.println("9 - Duplicate a Packet");
			}
		}
		if (operation == 6) {
			getAckpacketNumber(sc);
		}
		// if operation is a lost/delay/duplicate packet ask for the packet type
		if (operation == 7 || operation == 8 || operation == 9) {
			getPacketType(sc);
		} else {
			start();
		}
	}

	private void getAckpacketNumber(Scanner sc) {
		System.out.println("What ACK block number would you like to change (enter a number)?");
		while (true) {
			try {
				oldACK = sc.nextInt();
				// save the entered block number
				if (oldACK > -1) {
					break;
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid response, enter a valid number");
				System.out.println("What ACK block number would you like to change (enter a number)?");
			}
		}
		System.out.println("Enter the new ACK block number (enter a number)?");
		while (true) {
			try {
				newACK = sc.nextInt();
				// save the entered block number
				if (newACK > -1) {
					break;
				}
			} catch (NumberFormatException e) {
				System.out.println("Invalid response, enter a valid number");
				System.out.println("Enter the new ACK block number (enter a number)?");
			}
		}
	}

	/**
	 * 
	 * Method used to get the packet type for operation 6,7,8.
	 * 
	 */
	public void getPacketType(Scanner sc) {
		// check if operation is 7,8,9
		if (operation == 7 || operation == 8 || operation == 9) {
			System.out.println("What type of request would you like to lose/delay/duplicate?");
			System.out.println("RRQ (R)");
			System.out.println("WRQ (W)");
			// loop till valid entry
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

	/**
	 * 
	 * Method used to get the packet number from user
	 * 
	 */
	public void getPacketNumber(Scanner sc) {
		// make sure packet is a rrq or wrq
		if (requestPacketType == 1 || requestPacketType == 2) {
			System.out.println("What type of packet would you like to lose/delay/duplicate?");
			System.out.println("DATA (D)");
			System.out.println("ACK (A)");
			// loop till valid reponse given
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

	/**
	 * 
	 * Main method for error operation
	 * 
	 */
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

			if (clientAddress == null) {
				clientAddress = Packet.GetAddress();
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
			if (!filetransfer && operation < 5 && operation > -1) {
				putError(Packet, operation, sendReceiveSocket, 69, serverAddress);
				// if operation is for change port number
			} else if (filetransfer && operation == 5 && Packet.GetPacketNum() == 0) {
				Packet p = Packet;
				putError(Packet, operation, sendReceiveSocket, ServerPort, serverAddress);
				try {
					helper.sendPacket(p, sendReceiveSocket, address, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// if operation is for change ACK block number
			} else if (filetransfer && operation == 6 && filetransfer) {
				putError(Packet, operation, sendReceiveSocket, ServerPort, serverAddress);
			} else if (!filetransfer) {
				try {
					helper.sendPacket(Packet, sendReceiveSocket, address, 69);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// operation is for delay, duplicate, loss packets
			} else if (filetransfer && operation >= 7 && operation <= 9) {
				putError(Packet, operation, sendReceiveSocket, ServerPort, serverAddress);
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
			if (filetransfer && operation == 5 && Packet.GetPacketNum() == 0) {
				Packet p = Packet;
				putError(Packet, operation, sendSocket, ClientPort, clientAddress);
				operation = -1;
				try {
					helper.sendPacket(p, sendSocket, clientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (filetransfer && operation >= 7 && operation <= 9) {
				putError(Packet, operation, sendSocket, ClientPort, clientAddress);
			} else if (filetransfer && operation == 6) {
				putError(Packet, operation, sendSocket, ClientPort, clientAddress);
			} else {
				try {
					helper.sendPacket(Packet, sendSocket, clientAddress, ClientPort);
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
	public void putError(Packet newPacket, int userInput, DatagramSocket soc, int port, InetAddress addr) {

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
		// used for filename case
		byte[] newData;

		switch (userInput) {
		case 0: // standard operation no changes to packets or errors, etc..

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

		case 4: // datapacket is greater than 512bytes
			// create new data
			newData = new byte[522];
			// copy new data to old data
			System.arraycopy(data, 0, newData, 0, data.length);
			newData[newData.length - 1] = 0;
			newData[newData.length - 2] = 1;

			// New Packet
			Packet = new Packet(3, newData, newPacket.GetPacketNum());
			System.out.println("Modified Packet" + helper.byteToString(Packet.GetData()) + "\n");

			// send Packet to server
			try {
				helper.sendPacket(Packet, soc, address, port);
			} catch (IOException e5) {
				e5.printStackTrace();
			}

			this.operation = -1;

			break;

		case 5: // Change the Socket Invalid TID and send to the server.
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

			this.operation = -1;

			break;

		// change ack block num
		case 6:
			// check to make sure packet hasnt been sent already
			if (!(Packet.GetInquiry() == 4 && Packet.GetPacketNum() == oldACK)) {
				try {
					helper.sendPacket(newPacket, soc, address, port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			System.out.println("Changing the ACK block number " + oldACK + "  to " + newACK);

			// create a new ack packet
			Packet = new Packet(4, newACK);
			// send that packet
			try {
				helper.sendPacket(Packet, soc, address, port);
			} catch (IOException e4) {
				e4.printStackTrace();
			}

			this.operation = -1;

			break;

		case 7: // Lose a packet

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
				try {
					helper.sendPacket(p1, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
				soc = sendReceiveSocket;
				sender = "Server";
			} else { // sending to the server
				try {
					helper.sendPacket(p1, sendSocket, clientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
				soc = sendSocket;
				sender = "Client";
			}

			// sender times out
			System.out.println("receiving Message from " + sender + " time out");
			Packet = helper.receivePacket(soc);

			if (port == ClientPort) {
				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, clientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.operation = -1;

			break;

		case 8: // Delaying a Packet

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
					helper.sendPacket(Packet, sendSocket, clientAddress, ClientPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					helper.sendPacket(p1, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					helper.sendPacket(Packet, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					helper.sendPacket(p1, sendSocket, clientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.operation = -1;
			break;

		case 9: // Duplicating packets

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
					helper.sendPacket(Packet, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				System.out.println("Receiving packet from server");
				Packet = helper.receivePacket(sendReceiveSocket);

				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, clientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (port == ClientPort) {
				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (port == ServerPort && ((requestPacketType == 1 && errorPacketType == 4)
					|| (requestPacketType == 2 && errorPacketType == 3))) { // sending to the
				// server
				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, clientAddress, ClientPort);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				System.out.println("Receiving packet from client");
				Packet = helper.receivePacket(sendSocket);

				System.out.println("Sending packet to server");
				try {
					helper.sendPacket(Packet, sendReceiveSocket, serverAddress, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("Sending packet to client");
				try {
					helper.sendPacket(Packet, sendSocket, clientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.operation = -1;
			break;

		}
	}// End of Method

	private void setServerAddress(InetAddress addr) {
		serverAddress = addr;
	}

	public static void main(String args[]) {
		// ask user if they want verbose
		boolean verbose;
		boolean running = true;
		InetAddress LocalAddress = null;
		try {
			LocalAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		InetAddress ServerAddress = LocalAddress;
		Scanner sc = new Scanner(System.in);
		System.out.println("----ERRORSIM RUNNING----");
		System.out.println("");
		System.out.println("");
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
		boolean isLocal;
		while (true) {
			System.out.println("Would you like to run locally (Y/N)?");

			String input = sc.nextLine();
			if (input.toUpperCase().equals("Y")) {
				isLocal = true;
				break;
			}
			if (input.toUpperCase().equals("N")) {
				isLocal = false;
				break;
			}
			System.out.println("Invalid Mode! Select either 'Y'(Yes), 'N'(No)");
		}
		while (true && !isLocal) {
			System.out.println("Please enter the server address:");

			String input = sc.nextLine();

			try {
				ServerAddress = InetAddress.getByName(input);
				try {
					if (ServerAddress.isReachable(5000)) {
						System.out.println("Address is valid.\n");
						break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (UnknownHostException e) {
				System.out.println("Failed to Ping Address.");
			}
			System.out.println("Invalid Address.\n");
		}
			while (running) {
				TFTPErrorSim TFTPErrorSim = new TFTPErrorSim(verbose);
				TFTPErrorSim.setServerAddress(ServerAddress);
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

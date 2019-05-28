package iteration2;

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

	private DatagramSocket ClientSendandReceiveSocket, ServerSendandReceiveSocket;
	private DatagramPacket receivePacket, sendPacket;
	private TFTPHelper helper;
	private static InetAddress ClientAddress, ServerAddress;
	private String directory;
	private int ClientPort, ServerPort;
	private boolean verbose;
	private int operation = -1;
	private int errorPacketType = 0;	//1=RRQ, 2=WRQ, 3=DATA, 4=ACK
	private int packetBlockNumber = 0;

	public TFTPErrorSim(boolean verbose) {
		this.verbose = verbose;
		helper = new TFTPHelper("Sim", verbose);
		try {
			// client send and receive socket
			ClientSendandReceiveSocket = new DatagramSocket(23);
			// server send and receive socket
			ServerSendandReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
		ServerPort = 69;
	}

	public void OPset() {
		Scanner sc2 = new Scanner(System.in);
		System.out.println("What type of operation would you like to perform ");
		System.out.println("Normal operation (0)");
		System.out.println("Lost a packet (1)");
		System.out.println("Delay a packet (2)");
		System.out.println("Duplicate a Packet (3)");
		while (true) {
			String input = sc2.nextLine();
			if (input.equals("0")) {
				operation = 0;
				break;
			} else if (input.equals("1")) {
				operation = 1;
			} else if (input.equals("2")) {
				operation = 2;
			} else if (input.equals("3")) {
				operation = 3;
			} 
			if(input.equals("1") || input.equals("2") || input.equals("3")) {
				//show the type of packet options
				packetType();
				//if the lost packet isn't RRQ or RRQ, ask for block number
				if(errorPacketType != 1 && errorPacketType != 2) {
					packetNumber();
				}
				break;
			}
			
			//if we reach here, an invalid response was entered
			System.out.println("Invalid response, choose again");
			System.out.println("Normal operation (0)");
			System.out.println("Lost a packet (1)");
			System.out.println("Delay a packet (2)");
			System.out.println("Duplicate a Packet (3)");
			System.out.println("What type of operation would you like to perform ");
		}
		start();
	}

	public void packetType() {
		Scanner sc3 = new Scanner(System.in);
		System.out.println("What type of packet would you like to lose/delay/duplicate?");
		System.out.println("RRQ (R)");
		System.out.println("WRQ (W)");
		System.out.println("DATA (D)");
		System.out.println("ACK (A)");
		while(true) {
			String packetType = sc3.nextLine();
			if(packetType.equals("R")) { 
				//losing RRQ
				errorPacketType = 1;
				break;
			} else if (packetType.equals("W")) {
				//losing WRQ
				errorPacketType = 2;
				break;
			} else if (packetType.equals("D")) {
				//losing DATA
				errorPacketType = 3;
				break;
			} else if (packetType.equals("A")) {
				//losing ACK
				errorPacketType = 4;
				break;
			} else {
				System.out.println("Invalid response, choose again");
				System.out.println("What type of packet would you like to lose/delay/duplicate?");
				System.out.println("RRQ (R)");
				System.out.println("WRQ (W)");
				System.out.println("DATA (D)");
				System.out.println("ACK (A)");
				
			}
		}
	}
	
	public void packetNumber() {
		Scanner sc4 = new Scanner(System.in);
		System.out.println("What block number would you like to lose/delay/duplicate (enter a number)?");
		
		while(true) {
			String blockNum = sc4.nextLine();
			try {
				Integer integer = Integer.parseInt(blockNum);
				//save the entered block number
				packetBlockNumber = integer;
			} catch (NumberFormatException e) {
				System.out.println("Invalid response, enter a valid number");
				System.out.println("What block number would you like to lose/delay/duplicate?");
			}
		}
	}
	
	public void start() {
		// get users operation
		for (;;) {
			
			byte[] data = new byte[Packet.PACKETSIZE];
			// wait on port for data
			Packet receivedPacket = helper.receivePacket(ClientSendandReceiveSocket);
			if (operation == 0) {
				// Do nothing send same packet
				// holding data
				ClientPort = receivedPacket.getPort();
				ClientAddress = receivedPacket.getAddress();
				// ASSUMING BOTH CLIENT AND SERVER ON SAME ADDRESS
				ServerAddress = receivedPacket.getAddress();
				String ClientFileName = receivedPacket.getFile();
				// tell user what file has been received
				if (verbose) {
					System.out.println(helper.name + ": received file: " + ClientFileName);
				}
				System.out.println(helper.name + ": Sending file to server");
				// Send Received Packet to Server
				try {
					helper.sendPacket(receivedPacket, ServerSendandReceiveSocket, ServerAddress, ServerPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
				// Wait for response from Server
				receivedPacket = helper.receivePacket(ServerSendandReceiveSocket);
				ServerAddress = receivedPacket.getAddress();
				ServerPort = receivedPacket.getPort();
				// Send Received Response to Client
				try {
					helper.sendPacket(receivedPacket, ClientSendandReceiveSocket, ClientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (operation == 1) {
				//TODO implement the 'lost a packet' option
				
				
				
			} else if (operation == 2) {
				//TODO implement the 'delay a packet' option
				
				
				
			} else if (operation == 3) {
				
			}
		}
	}

	public static void main(String args[]) {
		// ask user if they want verbose
		boolean verbose;
		boolean running = true;
		Scanner sc = new Scanner(System.in);
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

		while (running) {
			TFTPErrorSim TFTPErrorSim = new TFTPErrorSim(verbose);
			TFTPErrorSim.OPset();

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

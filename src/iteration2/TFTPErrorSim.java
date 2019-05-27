package iteration2;

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
				sc2.close();
				operation = 0;
				break;
			} else if (input.equals("1")) {
				sc2.close();
				operation = 1;
				break;
			} else if (input.equals("2")) {
				sc2.close();
				operation = 2;
				break;
			} else if (input.equals("3")) {
				sc2.close();
				operation = 3;
				break;
			}
			System.out.println("Invalid response, choose again");
			System.out.println("Normal operation (0)");
			System.out.println("Lost a packet (1)");
			System.out.println("Delay a packet (2)");
			System.out.println("Duplicate a Packet (3)");
			System.out.println("What type of operation would you like to perform ");
		}
		start();
	}

	public void start() {
		// get users operation
		for (;;) {
			if (operation == 0) {
				// Do nothing send same packet
				// holding data
				byte[] data = new byte[Packet.PACKETSIZE];
				// wait on port for data
				Packet receivedPacket = helper.receivePacket(ClientSendandReceiveSocket);
				ClientPort = receivedPacket.GetPort();
				ClientAddress = receivedPacket.GetAddress();
				// ASSUMING BOTH CLIENT AND SERVER ON SAME ADDRESS
				ServerAddress = receivedPacket.GetAddress();
				String ClientFileName = receivedPacket.GetFile();
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
				ServerAddress = receivedPacket.GetAddress();
				ServerPort = receivedPacket.GetPort();
				// Send Received Response to Client
				try {
					helper.sendPacket(receivedPacket, ClientSendandReceiveSocket, ClientAddress, ClientPort);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (operation == 1) {

			} else if (operation == 2) {

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

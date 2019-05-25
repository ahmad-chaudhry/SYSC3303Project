package iteration2;

import java.net.InetAddress;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class TFTPClient {
	private DatagramSocket socket;
	private TFTPHelper helper;
	private static InetAddress ServerAddress = null;
	private String directory;
	private int Port;

	public TFTPClient(int port, InetAddress address, String dir, boolean verbose) {
		helper = new TFTPHelper("Client", verbose);
		try {
			socket = new DatagramSocket();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		directory = dir;
		Port = port;
		ServerAddress = address;
	}

	private void start() {
		int operation;
		// total blocks to transmit
		int totalnumBlocks = 0;
		// current block
		int currentBlock = 0;
		// get users operation (RRQ or WRQ)
		Scanner sc2 = new Scanner(System.in);
		// loop till user gives valid response
		while (true) {
			System.out.println("Would you like to do a RRQ or WRQ operation: ");
			String input = sc2.nextLine();
			if (input.toUpperCase().equals("RRQ")) {
				operation = 1;
				break;
			} else if (input.toUpperCase().equals("WRQ")) {
				operation = 2;
				break;
			}
			System.out.println("Invalid response, choose \"RRQ\" or \"WRQ\": ");
		}

		// Operation for RRQ
		if (operation == 1) {
			// loop till right file is found
			String serverFile;
			String clientFile;
			while (true) {
				System.out.println("Enter the name of the file you want to access (Server): ");
				serverFile = sc2.nextLine();
				// perform a check on file existing
				File tempFile = new File(directory + "\\server\\" + serverFile);
				boolean exists = tempFile.exists();
				if (exists) {
					break;
				} else {
					System.out.println("Invalid file name");
				}
			}
			FileOutputStream Fout;
			// loop till new file is created
			while (true) {
				System.out.println("Enter the name of the file you want to save to (Client): ");
				clientFile = sc2.nextLine();
				// check if file already exists
				File tempFile = new File(directory + "\\client\\" + clientFile);
				boolean exists = tempFile.exists();
				if (!exists) {
					try {
						tempFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				} else {
					System.out.println("File already exists");
				}
			}

			System.out.println("Attempting to establish connection with Server");

			Packet request = new Packet(1, serverFile);
			try {
				helper.sendPacket(request, socket, ServerAddress, Port);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			Packet receive = helper.receivePacket(socket);
			
			System.out.println("we got to this point!");
			//need to implement receive packet above to get a packet back 

		} else if (operation == 2) {

		} else {
			System.out.println("Should not get to this line");
		}

	}

	public static void main(String[] args) {
		// try the main, if error occurs catch it
		try {
			// port to be used (normal vs test)
			int port;
			// user input for verbose mode
			boolean verbose;
			// checks if client will still run
			boolean running = true;
			// scanner used for user input
			Scanner sc = new Scanner(System.in);
			// holder for IP address
			InetAddress AddrHolder;
			// loop while no break
			while (true) {
				System.out.println("Run client with verbose mode (Y/N)?");
				// get users input
				String input = sc.nextLine();
				// if user wants verbose
				if (input.toUpperCase().equals("Y")) {
					verbose = true;
					break;
				} else if (input.toUpperCase().equals("N")) {
					verbose = false;
					break;
				}
				// request user to enter valid input
				System.out.println("Mode not valid, please choose either \"Y\" or \"N\" for verbose");
			}
			// this loops forever unless user closes client
			while (running) {
				// ask user if they want to run normal or test mode
				while (true) {
					System.out.println("Do you want to run Normal or Test mode (Normal/Test): ");
					String input = sc.nextLine();
					if (input.toUpperCase().equals("NORMAL")) {
						port = 69;
						break;
					}
					if (input.toUpperCase().equals("TEST")) {
						port = 23;
						break;
					}
					System.out.println("Mode not valid, please choose either \"Normal\" or \"Test\" for mode");
				}
				// ask user if they want to run normal or test mode
				while (true) {
					System.out.println("Will you be running on local computer? (Y/N): ");
					String input = sc.nextLine();
					if (input.toUpperCase().equals("Y")) {
						AddrHolder = InetAddress.getLocalHost();
						break;
					}
					if (input.toUpperCase().equals("N")) {
						System.out.println("Enter the IP address: ");
						AddrHolder = InetAddress.getByName(sc.nextLine());
						break;
					}
					System.out.println("Mode not valid, please choose either \"Normal\" or \"Test\" for mode");
				}
				// get current directory to be used for saving and loading files
				String StartingDir = new File("").getAbsoluteFile().toString();
				// Initialize new client
				TFTPClient c = new TFTPClient(port, AddrHolder, StartingDir, verbose);
				c.start();
				while (true) {
					System.out.println("Run another file? (Y/N)?");
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
				System.out.println();
			}
			System.out.println("Client has closed TFTP program");
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
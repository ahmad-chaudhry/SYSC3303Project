package iteration3;

import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/**
 * TFTPClient.java
 * 
 * Iteration 2:
 * @author Ahmad Chaudhry
 * 
 * 
 * 
 */
import java.net.*;
import java.util.Scanner;

public class TFTPClient {
	private DatagramSocket socket;
	private TFTPHelper helper;
	private static InetAddress ServerAddress = null;
	private String directory;
	private int Port;
	private boolean addrInit;

	/**
	 * Constructor for class
	 * 
	 * @param port    The port you want to connect to
	 * @param address the IP address (local in our case)
	 * @param dir     Where to access the directory (pre-determined, set to project
	 *                folder)
	 * @param verbose True for additional information, else false
	 */
	public TFTPClient(int port, InetAddress address, String dir, boolean verbose) {
		// initalize helper methods with name set to client
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
		addrInit = false;
	}

	/**
	 * default run class
	 * 
	 * @throws IOException
	 */
	private void start() throws IOException {
		int operation;

		// get users operation (RRQ or WRQ)
		Scanner sc2 = new Scanner(System.in);
		// loop till user gives valid response
		while (true) {
			System.out.println("Would you like to do a RRQ or WRQ operation: ");
			String input = sc2.nextLine();
			// checks answer
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
			String serverFile;
			String clientFile;
			// loop till right file is found
			// while (true) {
			System.out.println("Enter the name of the file you want to access (Server): ");
			serverFile = sc2.nextLine();

			// perform a check on file existing
			/*
			 * File tempFile = new File(directory + "\\server\\" + serverFile); boolean
			 * exists = tempFile.exists(); if (exists) { break; } else {
			 * System.out.println("Invalid file name"); } }
			 */
			FileOutputStream Fout = null;
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
					// put clientFile into a FileOutputStream named Fout
					try {
						Fout = new FileOutputStream(directory + "\\client\\" + clientFile);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					break;
				} else {
					System.out.println("File already exists");
				}

			}

			System.out.println("Sending file request to Server");

			// sent inital read request packet to server with filename
			Packet request = new Packet(1, serverFile);
			// sending packet
			try {
				helper.sendPacket(request, socket, ServerAddress, Port);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			Packet receive;
			boolean valid = true;
			int validPacketNum = -1;
			// while true continue receiving files from server
			while (true) {
				// receive data packets from server
				try {
					receive = recurreceive(socket, request);
				} catch (IOException e) {
					System.out.println("No response, ending transfer");
					return;
				}
				ServerAddress = receive.GetAddress();
				Port = receive.GetPort();
				addrInit = true;
				// check that packet is valid
				if (!helper.validPacket(receive, 3)) {
					if (receive.GetInquiry() != 5) {
						Packet EPacket = new Packet(5, 4, "invalid packet received");
						try {
							helper.sendPacket(EPacket, socket, ServerAddress, Port);
						} catch (IOException e) {
							System.out.println("Could not send invalid packet");
						}
					}
					return;
				}

				if (receive.GetInquiry() == 5) {
					/**
					 * if (receive.getErrorCode() == 1) {
					 * System.out.println(receive.getErrorMssg()); break; } else if
					 * (receive.getErrorCode() == 2) { System.out.println(receive.getErrorMssg());
					 * break; } else if (receive.getErrorCode() == 6) {
					 * System.out.println(receive.getErrorMssg()); break; } else
					 */
					if (receive.getErrorCode() == 4) {
						System.out.println(receive.getErrorMssg());
						break;
					}
				}

				// make sure packet is a data packet
				if (receive.GetInquiry() == 3) {
					if (receive.GetPacketNum() == validPacketNum) {
						valid = false;
						System.out.println("Invalid packet received, will wait for another");
					} else if (receive.GetPacketNum() != validPacketNum) {
						valid = true;
					}
					if (valid) {
						// if packet received is 512 bytes then you know more data is coming
						if (receive.dataLength() == 512) {
							// write data to file using helper method
							helper.WriteData(Fout, receive.GetData());
							// create ack package with received packet number to send back to sender
							Packet ack = new Packet(4, receive.GetPacketNum());
							// send off packet
							try {
								validPacketNum = receive.GetPacketNum();
								helper.sendPacket(ack, socket, ServerAddress, Port);
							} catch (IOException e) {
								e.printStackTrace();
								System.exit(1);
							}
							// packet is between 0 and 512 so we know this is the last packet
						} else if (receive.dataLength() > 0 && receive.dataLength() < 512) {
							// write the packet to file
							helper.WriteData(Fout, receive.GetData());
							// create last ack packet
							Packet ack = new Packet(4, receive.GetPacketNum());
							// send last ack packet to sender
							try {
								validPacketNum = receive.GetPacketNum();
								helper.sendPacket(ack, socket, ServerAddress, Port);
							} catch (IOException e) {
								e.printStackTrace();
								System.exit(1);
							}
							break;
						} else {
							System.out.println("no more packets to receives");
							break;
						}
					}
				}
			}
			try {
				Fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// WRITE REQUEST
		} else if (operation == 2)

		{
			String serverFile;
			String clientFile;
			int currentBlock = 0;
			int totalnumBlocks = 0;
			FileInputStream Fin;
			File file;
			// loop till right file is found
//			while (true) {
			System.out.println("Enter the name of the file you want to save to (Server): ");
			serverFile = sc2.nextLine();
			// check if file already exists
			/*
			 * File tempFile = new File(directory + "\\server\\" + serverFile); boolean
			 * exists = tempFile.exists(); if (!exists) { try { tempFile.createNewFile(); }
			 * catch (IOException e) { e.printStackTrace(); } break; } else {
			 * System.out.println("File already exists"); } }
			 */
			// loop till file is found that exists on client
			while (true) {
				System.out.println("Enter the name of the read file (Client): ");
				clientFile = sc2.nextLine();
				// perform a check on file existing
				Fin = helper.OpenInputFile(directory + "\\client\\" + clientFile);
				file = new File(directory + clientFile);
				if (Fin == null) {
					System.out.println("the file does not exist, try again");
					// limit is 100,000 bytes or 100 kilobytes
				} else if (file.length() > 100000) {
					System.out.println("this file is too large, please use a smaller file (100 kilobytes max size)");
				} else {
					break;
				}
			}
			file = new File(directory + clientFile);
			System.out.println();
			// max blocks needed
			totalnumBlocks = (int) (file.length() / Packet.DATASIZE);
			// output if verbose
			if (helper.verbose) {
				System.out.println(helper.name + ": File located, starting transfer of " + totalnumBlocks + " blocks.");
			}

			System.out.println("Sending Data to Server");
			// send first packet WRQ to the server
			Packet request = new Packet(2, serverFile);
			// send the packet
			try {
				// uses helper method to send
				helper.sendPacket(request, socket, ServerAddress, Port);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			// receive ack packet from server to write information
			Packet receive1 = recurreceive(socket, request);
			ServerAddress = receive1.GetAddress();
			Port = receive1.GetPort();
			addrInit = true;

			if (!helper.validPacket(receive1, 4)) {
				if (receive1.GetInquiry() != 5) {
					Packet EPacket = new Packet(5, 4, "invalid packet received");
					try {
						helper.sendPacket(EPacket, socket, ServerAddress, Port);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return;
			}

			if (receive1.GetInquiry() == 5) {
				/**
				 * if (receive1.getErrorCode() == 1) {
				 * System.out.println(receive1.getErrorMssg()); return; } else if
				 * (receive1.getErrorCode() == 2) { System.out.println(receive1.getErrorMssg());
				 * return; } else if (receive1.getErrorCode() == 6) {
				 * System.out.println(receive1.getErrorMssg()); return; } else
				 */
				if (receive1.getErrorCode() == 4) {
					System.out.println(receive1.getErrorMssg());
					return;
				}
			}

			boolean valid = true;
			// File transfer loop;
			// keep sending data till you've sent all blocks
			while (currentBlock <= totalnumBlocks) {
				Packet dpacket = null;
				Packet receive;
				if (valid) {
					// get data from file
					byte[] blockData = helper.ReadData(Fin, currentBlock, Packet.DATASIZE);
					// create data packet to send
					dpacket = new Packet(3, blockData, currentBlock);
					// send packet
					try {
						helper.sendPacket(dpacket, socket, ServerAddress, Port);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// wait for receive packet
				try {
					receive = recurreceive(socket, dpacket);
				} catch (IOException e) {
					System.out.println("no response, ending transfer");
					return;
				}

				if (!helper.validPacket(receive1, 4)) {
					if (receive1.GetInquiry() != 5) {
						Packet EPacket = new Packet(5, 4, "invalid packet received");
						try {
							helper.sendPacket(EPacket, socket, ServerAddress, Port);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return;
				}
				// check that it is ack packet
				if (receive.GetPacketNum() == currentBlock) {
					currentBlock++;
				} else {
					System.out.println("packet ack already receieved, wait for new one");
					valid = false;
				}
			}
		} else {
			System.out.println("Should not get to this line");
		}

	}

	private Packet recurreceive(DatagramSocket soc, Packet resend) throws IOException {
		Packet receive = null;
		try {
			receive = helper.receivePacket(soc, helper.timeout);
		} catch (IOException e) {
			if (helper.verbose)
				System.out.println("Socket timed out, retrying...");
			if (resend != null) {
				System.out.println("Resending last packet...");
				helper.sendPacket(resend, soc, ServerAddress, Port);
			}
			if (helper.retries > 0) {
				helper.retries = helper.retries - 1;
				return recurreceive(soc, resend);
			}
			throw e;
		}
		if ((!checkAddress(receive)) && addrInit) {
			Packet ERR = new Packet(5, 5, "Packet received from unknown sender.");
			helper.sendPacket(ERR, soc, receive.GetAddress(), receive.GetPort());
			System.out.println("Listenning for the connection again...");
			return recurreceive(soc, resend);
		} else
			return receive;
	}

	private boolean checkAddress(Packet P) {
		if (P.GetPort() == Port && P.GetAddress().equals(ServerAddress))
			return true;
		return false;
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
			System.out.println("This is the CLIENT");
			System.out.println("enter \"quit\" at anytime to quit");
			System.out.println();
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
				} else if (input.toLowerCase().equals("quit")) {
					System.out.println("Client is shutting down");
					System.exit(0);
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
					} else if (input.toLowerCase().equals("quit")) {
						System.out.println("Client is shutting down");
						System.exit(0);
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
					if (input.toLowerCase().equals("quit")) {
						System.out.println("Client is shutting down");
						System.exit(0);
					}
					System.out.println("Mode not valid, please choose either \"Normal\" or \"Test\" for mode");
				}
				// get current directory to be used for saving and loading files
				String StartingDir = new File("").getAbsoluteFile().toString();
				// Initialize new client
				TFTPClient c = new TFTPClient(port, AddrHolder, StartingDir, verbose);
				c.start();
				while (true) {
					System.out.println("Run another file? (Y/N) (N will close client)?");
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
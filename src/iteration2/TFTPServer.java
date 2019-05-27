package iteration2;

/**
 * TFTPServer.java 
 * 
 * Iteration 2
 * 
 * @author ahmad chaudhry
 * 
 */
import java.io.File;

/**
 * TFTPServer.java
 * 
 * Iteration 2:
 * @author Ahmad Chaudhry
 * 
 * 
 * 
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TFTPServer {

	/**
	 * Default run for server, will generate a master thread
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// ask user if they want verbose
		boolean verbose;
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
		String StartingDir = new File("").getAbsoluteFile().toString();
		MasterThread MT = new MasterThread(StartingDir + "\\server\\", verbose);

		MT.start();

		while (true) {
			System.out.println("Enter \"Close\" to close the server\n");
			String input = sc.nextLine();
			if (input.toUpperCase().equals("CLOSE")) {
				break;
			}
		}
		MT.Stop();
		sc.close();
	}
}

//ServerMaster, awaits for connection and passes on to WorkerHandler;
class MasterThread extends Thread {
	private DatagramPacket workerPacket;
	private boolean running;
	private boolean verbose;
	private DatagramSocket socket;
	private TFTPHelper helper;

	private ArrayList<WorkerHandler> workers;
	private String directory;

	public MasterThread(String directory, boolean verbose) {
		workers = new ArrayList<WorkerHandler>();
		helper = new TFTPHelper("Server", verbose);
		// create a datagramsocket for receiving
		try {
			socket = new DatagramSocket(69);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		if (verbose) {
			System.out.println(helper.name + ": socket creation successful");
		}
		running = true;
		this.verbose = verbose;
		this.directory = directory;
	}

	/**
	 * This is the main run method for the Master thread
	 */
	public void run() {
		// server will continue until stopped and wait for remaining threads to finish
		while (running || !allDone()) {
			byte[] receive = new byte[Packet.PACKETSIZE];
			workerPacket = new DatagramPacket(receive, receive.length);
			try {
				socket.setSoTimeout(500);
				socket.receive(workerPacket);
				if (verbose) {
					System.out.println(helper.name + ": connection established, passing to worker. \n");
					System.out.println();
					System.out.println(
							helper.name + ": The bytes recieved are:\n" + Arrays.toString(workerPacket.getData()));
				}
				handlePacket(new Packet(workerPacket.getData()), workerPacket.getPort(), workerPacket.getAddress());
				// System.out.println("WE HAVE REACHED THIS STAGE");
			} catch (IOException e) {
				socket.close();
				try {
					socket = new DatagramSocket(69);
				} catch (SocketException se) {
					se.printStackTrace();
					System.exit(1);
				}
			}

		}
		socket.close();
		System.out.println(helper.name + ": All workers have completed, exiting.");
		System.exit(1);
	}

	/**
	 * Method to stop master Thread, this will allow leftover works to still
	 * complete
	 */
	public void Stop() {
		running = false;
		if (verbose) {
			System.out.println(helper.name + ": Closing server, waiting for workers to complete");
		}
	}

	private boolean allDone() {
		for (int i = 0; i < workers.size(); i++)
			if (!workers.get(i).isDone())
				return false;
		return true;
	}

	// Sends the packet to a worker thats active and has the same address, else
	// create a new one;
	private void handlePacket(Packet request, int port, InetAddress address) {
		// Locates appropriate Worker to send packet to;
		for (int i = 0; i < workers.size(); i++) {
			if (!workers.get(i).isDone()) {
				if (workers.get(i).ClientAddress.equals(address) && workers.get(i).Port == port) {
					workers.get(i).passReq(request);
					return;
				}
			}
		}
		// If the program is still running, accepts a new request;
		if (running)
			workers.add(new WorkerHandler(port, address, request, directory, verbose));
	}
}

//This class wraps around the ServerWorker class so that MetaData can be retrieved
class WorkerHandler {
	private ServerWorker worker;
	private BlockingQueue<Packet> bQueue;
	public int Port;
	public InetAddress ClientAddress;
	private String Directory;

	// Constructor for worker;
	public WorkerHandler(int Port, InetAddress clientAddress, Packet workerpacket, String dir, boolean verbose) {
		bQueue = new ArrayBlockingQueue<Packet>(10);
		this.Port = Port;
		ClientAddress = clientAddress;
		Directory = dir;
		worker = new ServerWorker(Port, clientAddress, workerpacket, verbose, Directory, bQueue);
		worker.start();

	}

	// Wait for ServerWorker Thread to complete;
	public void Wait() {
		try {
			worker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Checks if ServerWorker Thread to complete;
	public boolean isDone() {
		if (worker.getState() == Thread.State.TERMINATED)
			return true;
		return false;
	}

	// Passes the request to the thread;
	public void passReq(Packet request) {
		try {
			bQueue.put(request);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

//Worker thread that handles a client request;
class ServerWorker extends Thread {
	private TFTPHelper helper;
	private Packet workingPacket;
	private InetAddress Address;
	private DatagramSocket socket;
	private int Port;
	private String Directory;
	private BlockingQueue<Packet> bQueue;

	// Constructor;
	public ServerWorker(int Port, InetAddress Address, Packet request, boolean verbose, String dir,
			BlockingQueue<Packet> Queue) {
		this.Port = Port;
		this.Address = Address;
		workingPacket = request;
		bQueue = Queue;
		Directory = dir;
		helper = new TFTPHelper("ServerWorker@" + Port, verbose);
		try {
			socket = new DatagramSocket();
		} catch (SocketException se) {
			System.out.println(helper.name + ": Failed to create Socket.");
			System.exit(1);
		}
	}

	// Main ServerWorker logic;
	public void run() {
		System.out.println();
		FileInputStream FIn;
		if (workingPacket.GetInquiry() == 1) {
			System.out.println(workingPacket.GetFile());
			// Read request;
			FIn = helper.OpenInputFile(Directory + workingPacket.GetFile());
			if (FIn == null) {
				System.exit(1);
			}
			int totalnumBlocks = 0;
			int currentBlock = 0;

			try {
				totalnumBlocks = (int) (FIn.getChannel().size() / Packet.DATASIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (helper.verbose) {
				System.out.println(helper.name + ": File located, starting transfer of " + totalnumBlocks + " blocks.");
			}

			// File transfer loop;
			while (currentBlock <= totalnumBlocks) {
				byte[] blockData = helper.ReadData(FIn, currentBlock, Packet.DATASIZE);
				Packet dpacket = new Packet(3, blockData, currentBlock);
				try {
					helper.sendPacket(dpacket, socket, Address, Port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Packet receive = recurreceive(socket);

				if (receive.GetInquiry() == 4) {
					currentBlock++;
				} else
					System.exit(1);
			}
			try {
				FIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// Write Request
			FileOutputStream FOut = null;
			try {
				FOut = new FileOutputStream(Directory + workingPacket.GetFile(), true);
			} catch (FileNotFoundException e3) {
				e3.printStackTrace();
			}
			
			Packet ack1 = new Packet(4, 0);
			try {
				helper.sendPacket(ack1, socket, Address, Port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			
			System.out.println(workingPacket.GetFile());
			System.out.println(socket.getLocalPort());
			System.out.println(socket.getLocalAddress());
			System.out.println(socket.getPort());
			System.out.println(socket.getInetAddress());

			Packet receive = helper.receivePacket(socket);

			while (true) {
				if (receive.GetInquiry() == 3) {
					if (receive.dataLength() == 512) {
						helper.WriteData(FOut, receive.GetData());
						Packet ack = new Packet(4, receive.GetPacketNum());
						try {
							helper.sendPacket(ack, socket, Address, Port);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}
					} else if (receive.dataLength() > 0 && receive.dataLength() < 512) {
						helper.WriteData(FOut, receive.GetData());
						Packet ack = new Packet(4, receive.GetPacketNum());
						try {
							helper.sendPacket(ack, socket, Address, Port);
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
			try {
				FOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(helper.name + ": File transfer done, worker thread done.\n");
	}

	private boolean checkAddress(Packet P) {
		if (P.GetPort() == Port && P.GetAddress().equals(Address))
			return true;
		return false;
	}

	private Packet recurreceive(DatagramSocket soc) {
		Packet rec = helper.receivePacket(soc);
		if (!checkAddress(rec)) {
			Packet ERR = new Packet(5, "Packet received from unknown sender.");
			try {
				helper.sendPacket(ERR, soc, rec.GetAddress(), rec.GetPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return recurreceive(soc);
		} else
			return rec;
	}
}

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
		// setup the master thread and initial directory location
		String StartingDir = new File("").getAbsoluteFile().toString();
		MasterThread MT = new MasterThread(StartingDir + "\\server\\", verbose);
		// start the master thread
		MT.start();
		// loop until user closes server
		while (true) {
			System.out.println("Enter \"Close\" to close the server\n");
			String input = sc.nextLine();
			if (input.toUpperCase().equals("CLOSE")) {
				break;
			}
		}
		// stop the thread and close the sc reader
		MT.Stop();
		sc.close();
	}
}

/**
 * The master thread class that extends thread
 *
 */
class MasterThread extends Thread {
	private DatagramPacket workerPacket;
	private boolean running;
	private boolean verbose;
	private DatagramSocket socket;
	private TFTPHelper helper;

	private ArrayList<WorkerHandler> workers;
	private String directory;

	/**
	 * default constructor for master thread
	 * 
	 * @param directory
	 * @param verbose
	 */
	public MasterThread(String directory, boolean verbose) {
		// create a array of workers to track
		workers = new ArrayList<WorkerHandler>();
		// setup helper class for methods
		helper = new TFTPHelper("Server", verbose);
		// create a datagramsocket for receiving
		try {
			// server socket
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
		while (running || !workersDone()) {
			byte[] receive = new byte[Packet.PACKETSIZE];
			// create a worker packet to pass off to workers
			workerPacket = new DatagramPacket(receive, receive.length);
			try {
				// socket will timeout after 500
				socket.setSoTimeout(500);
				// receive packet
				socket.receive(workerPacket);
				if (verbose) {
					System.out.println(helper.name + ": connection established, passing to worker. \n");
					System.out.println();
					System.out.println(
							helper.name + ": The bytes recieved are:\n" + Arrays.toString(workerPacket.getData()));
				}
				// create a new packet for worker
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

	/**
	 * check if any workers are left
	 * 
	 * @return true or false
	 */
	private boolean workersDone() {
		for (int i = 0; i < workers.size(); i++)
			if (!workers.get(i).isDone())
				return false;
		return true;
	}

	/**
	 * Send the packet to a worker that is active with the same address or then make
	 * a new worker
	 * 
	 * @param request
	 * @param port
	 * @param address
	 */
	private void handlePacket(Packet request, int port, InetAddress address) {
		// find a worker to give it to
		for (int i = 0; i < workers.size(); i++) {
			if (!workers.get(i).isDone()) {
				// check if any works can take it
				if (workers.get(i).ClientAddress.equals(address) && workers.get(i).Port == port) {
					workers.get(i).passReq(request);
					return;
				}
			}
		}
		// make a new worker to take it
		if (running)
			workers.add(new WorkerHandler(port, address, request, directory, verbose));
	}
}

/**
 * Class for workerhandler, used to encapsulate the serverwork threads
 * 
 */
class WorkerHandler {
	private ServerWorker worker;
	private BlockingQueue<Packet> bQueue;
	public int Port;
	public InetAddress ClientAddress;
	private String Directory;

	/**
	 * default constructor for workerhandler
	 * 
	 * @param Port
	 * @param clientAddress
	 * @param workerpacket
	 * @param dir
	 * @param verbose
	 */
	public WorkerHandler(int Port, InetAddress clientAddress, Packet workerpacket, String dir, boolean verbose) {
		bQueue = new ArrayBlockingQueue<Packet>(10);
		this.Port = Port;
		ClientAddress = clientAddress;
		Directory = dir;
		// make a new worker thread to take the packet
		worker = new ServerWorker(Port, clientAddress, workerpacket, verbose, Directory, bQueue);
		// start the worker thread
		worker.start();

	}

	/**
	 * Method to wait fora serverworker to finish
	 */
	public void Wait() {
		try {
			worker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to check if a serverwork is done
	 * 
	 * @return true or false
	 */
	public boolean isDone() {
		if (worker.getState() == Thread.State.TERMINATED)
			return true;
		return false;
	}

	/**
	 * Method to pass a packet to a already working thread that can take it
	 * 
	 */
	public void passReq(Packet request) {
		try {
			bQueue.put(request);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/**
 * Class for serverworker that is a thread
 * 
 */
class ServerWorker extends Thread {
	private TFTPHelper helper;
	private Packet workingPacket;
	private InetAddress Address;
	private DatagramSocket socket;
	private int Port;
	private String Directory;
	private BlockingQueue<Packet> bQueue;

	/**
	 * the default constructor for the class
	 * 
	 * @param Port
	 * @param Address
	 * @param request
	 * @param verbose
	 * @param dir
	 * @param Queue
	 */
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

	/**
	 * The run for the serverworker thread used for either a RRQ or WRQ 
	 * 
	 */
	public void run() {
		System.out.println();
		System.out.println();
		//create a new input file stream
		FileInputStream FIn;
		//check if the packet received is for a RRQ
		if (workingPacket.getInquiry() == 1) {
			//print the name of the file
			System.out.println(workingPacket.getFile());
			//put file into a filestream
			FIn = helper.OpenInputFile(Directory + workingPacket.getFile());
			//make sure file is not null
			if (FIn == null) {
				System.exit(1);
			}
			//initalize total blocks and current block
			int totalnumBlocks = 0;
			int currentBlock = 0;

			//find the total number of blocks for the file to transfer
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
				//create a block of data from file
				byte[] blockData = helper.ReadData(FIn, currentBlock, Packet.DATASIZE);
				//put it into a data packet
				Packet dpacket = new Packet(3, blockData, currentBlock);
				//send of that data packet
				try {
					helper.sendPacket(dpacket, socket, Address, Port);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//wait to receive a ack packet
				Packet receive = helper.receivePacket(socket);
				//check that it is a ACK packet
				if (receive.getInquiry() == 4) {
					//continue to move to the next block if ACK packet
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
				FOut = new FileOutputStream(Directory + workingPacket.getFile(), true);
			} catch (FileNotFoundException e3) {
				e3.printStackTrace();
			}
			//send a ACK packet for WRQ
			Packet ack1 = new Packet(4, 0);
			try {
				helper.sendPacket(ack1, socket, Address, Port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			//wait for DATA packet 
			Packet receive = helper.receivePacket(socket);
			//loop till file is retrieved 
			while (true) {
				//check that packet is a DATA packet
				if (receive.getInquiry() == 3) {
					//check if size of packet is full so we know more packets are coming
					if (receive.dataLength() == 512) {
						//write data to file
						helper.WriteData(FOut, receive.getData());
						//send pack a ACK packet
						Packet ack = new Packet(4, receive.getPacketNum());
						try {
							helper.sendPacket(ack, socket, Address, Port);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}
						//we know that if DATA packet is between 0 and 512 this is the last packet
					} else if (receive.dataLength() > 0 && receive.dataLength() < 512) {
						//write data to file
						helper.WriteData(FOut, receive.getData());
						//send last ACK packet
						Packet ack = new Packet(4, receive.getPacketNum());
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
}

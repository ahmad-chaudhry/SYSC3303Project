package iteration5;

/**
 * TFTPServer.java 
 * 
 * Iteration 4
 * 
 * 
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
		System.out.println("----SERVER RUNNING----");
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
		// create a new input file stream
		BufferedInputStream FIn;
		// check that packet is for RRQ or WRQ
		if (workingPacket.GetInquiry() != 1 && workingPacket.GetInquiry() != 2) {
			Packet Epacket = new Packet(5, 4, "invalid request");
			try {
				helper.sendPacket(Epacket, socket, Address, Port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket.close();
			return;
			// Check that filename is not blank
		} else if (workingPacket.GetFile().equals("")) {
			Packet Epacket = new Packet(5, 4, "no filename");
			try {
				helper.sendPacket(Epacket, socket, Address, Port);
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket.close();
			return;
		}
		// check if the packet received is for a RRQ
		if (workingPacket.GetInquiry() == 1) {
			// print the name of the file
			System.out.println(workingPacket.GetFile());
			File file = new File(Directory + workingPacket.GetFile());
			try {
				// put file into a filestream
				FIn = new BufferedInputStream(new FileInputStream(file));
			} catch (SecurityException e) {
				System.out.println("File error cannot access");
				Packet Epacket = new Packet(5, 2, "Access violation");
				try {
					helper.sendPacket(Epacket, socket, Address, Port);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				socket.close();
				return;
			} catch (FileNotFoundException e) {
				if (file.exists()) {
					System.out.println("File error cannot access");
					Packet Epacket = new Packet(5, 2, "Access violation");
					try {
						helper.sendPacket(Epacket, socket, Address, Port);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					socket.close();
					return;
				}
				System.out.println("File not found");
				Packet Epacket = new Packet(5, 1, "File not found");
				try {
					helper.sendPacket(Epacket, socket, Address, Port);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				socket.close();
				return;
			}

			// initalize total blocks and current block
			int totalnumBlocks = 0;
			int currentBlock = 0;

			totalnumBlocks = (int) (file.length() / Packet.DATASIZE);

			if (file.length() > 100000) {
				Packet ePacket = new Packet(0, "File is too big to be sent.");
				try {
					helper.sendPacket(ePacket, socket, Address, Port);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				socket.close();
				try {
					FIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			if (helper.verbose) {
				System.out.println(helper.name + ": File located, starting transfer of " + totalnumBlocks + " blocks.");
			}

			boolean valid = true;
			// wait to receive a ack packet
			Packet receive;
			// File transfer loop;
			while (currentBlock <= totalnumBlocks) {
				Packet dpacket = null;
				if (valid) {
					// create a block of data from file
					byte[] blockData = helper.ReadData(FIn, currentBlock, Packet.DATASIZE);
					// put it into a data packet
					dpacket = new Packet(3, blockData, currentBlock);
					// send of that data packet
					try {
						helper.sendPacket(dpacket, socket, Address, Port);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					receive = recurreceive(socket, dpacket);
				} catch (IOException e1) {
					System.out.println("timed out connection, thread closing");
					socket.close();
					try {
						FIn.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				if (!helper.validPacket(receive, 4)) {
					if (receive.GetInquiry() != 5) {
						Packet EPacket = new Packet(5, 4, "invalid packet received");
						try {
							helper.sendPacket(EPacket, socket, Address, Port);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					socket.close();
					return;
				}

				// check that it is a ACK packet
				if (receive.GetPacketNum() == currentBlock) {
					// continue to move to the next block if ACK packet
					currentBlock++;
					valid = true;
				} else {
					System.out.println("ack packet invalid, waiting for another");
					valid = false;
				}
			}
			try {
				FIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else

		{
			// Write Request
			BufferedOutputStream FOut;
			File fdirectory = new File(Directory + workingPacket.GetFile());
			long spaceAvailable = new File(Directory).getUsableSpace();

			if (!fdirectory.exists()) {
				try {
					fdirectory.createNewFile();
				} catch (IOException e) {
					System.out.println("Cannot create file");
				}
			} else {
				System.out.println("File already exists");

				Packet Epacket = new Packet(5, 6, "file already exists");
				try {
					helper.sendPacket(Epacket, socket, Address, Port);
				} catch (IOException e) {
					e.printStackTrace();
				}

				socket.close();
				return;
			}
			FOut = helper.OpenOFile(Directory + workingPacket.GetFile());
			if (FOut == null)
				System.out.println("File already exists! Please use another!");

			// send a ACK packet for WRQ
			Packet ack1 = new Packet(4, 0);
			try {
				helper.sendPacket(ack1, socket, Address, Port);
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			Packet receive;
			boolean valid = true;
			int validPacketNum = -1;
			// loop till file is retrieved
			while (true) {
				// wait for DATA packet
				try {
					receive = recurreceive(socket, ack1);
				} catch (IOException e1) {
					System.out.println("timed out connection, thread closing");
					socket.close();
					try {
						FOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				// check that the packet received is valid (DATA PACKET)
				if (!helper.validPacket(receive, 3)) {
					if (receive.GetInquiry() != 5) {
						Packet EPacket = new Packet(5, 4, "invalid packet received");
						try {
							helper.sendPacket(EPacket, socket, Address, Port);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return;
				}

				// Check if file is larger than space available
				if (receive.GetData().length > spaceAvailable) {
					System.out.println("Disk full or allocation exceeded.");
					Packet EPacket = new Packet(5, 3, "Disk full or allocation exceeded.");
					try {
						helper.sendPacket(EPacket, socket, Address, Port);
					} catch (IOException e) {
						e.printStackTrace();
					}
					socket.close();
					try {
						FOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}

				if (receive.GetInquiry() == 5) {
					if (receive.getErrorCode() == 1) {
						System.out.println(receive.getErrorMssg());
						break;
					} else if (receive.getErrorCode() == 2) {
						System.out.println(receive.getErrorMssg());
						break;
					} else if (receive.getErrorCode() == 6) {
						System.out.println(receive.getErrorMssg());
						break;
					} else

					if (receive.getErrorCode() == 4) {
						System.out.println(receive.getErrorMssg());
						break;
					}
				}

				if (receive.GetPacketNum() == validPacketNum) {
					valid = false;
					System.out.println("Invalid packet received, will wait for another");
				} else if (receive.GetPacketNum() != validPacketNum) {
					valid = true;
				}
				if (valid) {
					// check if size of packet is full so we know more packets are coming
					if (receive.dataLength() == 516) {
						// write data to file
						helper.WriteData(FOut, receive.GetData());
						// send pack a ACK packet
						Packet ack = new Packet(4, receive.GetPacketNum());
						try {
							helper.sendPacket(ack, socket, Address, Port);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}

						// we know that if DATA packet is between 0 and 512 this is the last packet
					} else if (receive.dataLength() >= 0 && receive.dataLength() < 516) {
						// write data to file
						helper.WriteData(FOut, receive.GetData());
						// send last ACK packet
						Packet ack = new Packet(4, receive.GetPacketNum());
						try {
							validPacketNum = receive.GetPacketNum();
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
		System.out.println();
		System.out.println("Enter \"Close\" to close the server\n");

	}

	/**
	 * 
	 * Method used for receving packet with timeout and re-try of packet 
	 *
	 */
	private Packet recurreceive(DatagramSocket soc, Packet resend) throws IOException {
		Packet receive = null;
		try {
			receive = helper.receivePacket(soc, helper.timeout);
		} catch (IOException e1) {
			if (helper.verbose)
				System.out.println("Socket timed out, retrying...");
			if (resend != null) {
				System.out.println("Resending last packet...");
				helper.sendPacket(resend, soc, Address, Port);
			}
			if (helper.retries > 0) {
				helper.retries = helper.retries - 1;
				return recurreceive(soc, resend);
			}
			throw e1;
		}
		if ((!checkAddress(receive))) {
			Packet ERR = new Packet(5, 5, "Packet received from unknown sender.");
			helper.sendPacket(ERR, soc, receive.GetAddress(), receive.GetPort());
			System.out.println("Listenning for the connection again...");
			return recurreceive(soc, resend);
		} else
			return receive;
	}

	/**
	 * 
	 * Method used to check that address of packet is the same as well as port
	 * 
	 */
	private boolean checkAddress(Packet P) {
		if (P.GetPort() == Port && P.GetAddress().equals(Address))
			return true;
		return false;
	}
}

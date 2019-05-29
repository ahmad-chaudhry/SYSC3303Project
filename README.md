# SYSC3303Project-Iteration 2

Adam Labelle 101038735
Johan Westeinde 101055222
Dominic Kocjan 100980801
Lyndon Lo 101030526
Ahmad Chaudhry 101003005



File Names and Setup:

Packet:

TFTPClient: This class is the client class where the user will choose what data to read/write. At any point, the user can enter 'quit' and the client will shutdown. Once we run the client, the user will be asked if they want to run the client in verbose mode (Y/N). Once they enter a valid response, they will be asked if they want to run in normal mode or test mode (Normal/Test). Once they enter a valid response, they will be asked if they will be running on the local computer (Y/N). All the options that the user enters will be saved and slightly alter the running of the Client, as the project outline explains.
Once they enter a valid response, they will be prompted to enter the request they wish to make a read request or a write request (RRQ or WRQ). From here there are two different paths to take. First, if the user enters RRQ, meaning they want to do a read request, they will be asked what file they want to access from the server. If they don't enter a valid file name (one that exists on the server side), they will be told they have entered an invalid file name and will be prompted to re-enter the name of the file they want to access. Once they enter a valid file name, they will be asked to enter the name of the file where they want to save it (on the client side). If they enter a file that already exists, they will be prompted to enter a different file name. The other option is that they enter WRQ, then they will be asked to enter the name of the file where they want to save to (on the server side). If they enter a file name that already exists, they will be prompted to enter a different file name. Once they enter a valid file name, they enter the name of the file they want to read (client side).

From here, the datagram packet that contains the RRQ or WRQ gets sent from a datagram socket and passed to the TFTPErrorSim, which then passes it on to the TFTPServer. The file transfer goes on to completion.

TFTPErrorSim: 

TFTPHelper:

TFTPServer: This is the class that represents the Server. First, it's prompted on if we would like to run it in verbose mode (Y/N). Once we enter a valid option, a socket of type MasterSocket gets created on port 69 and waits to receive datagrams. When datagrams come in, they get passed on to ServerWorker threads to deal with the data transfer.



Breakdown of responsibilities of each team member for this and previous iterations:

Iteration 1:
When we submitted iteration 1 we didn't have the file transfer working properly.

Johan: Created ClientConnectionThread class, the sendAndReceive() method in the Client class, created the class diagram
Dominic: Worked on the server handling & client connection threads, use cases.
Adam: Handled the shut down.
Lyndon: Created the scanners to prompt the user to enter the action they want to perform (read, write, quit) and the file name they wish to read/write to/from. Added multithreaded server.
Ahmad: Wasn't on our team for Iteration 1


Iteration 2:

Johan: Updated TFTPErrorSim class to ask for packet type and block number of the datagram packet that we want to test. Added client shutdown option throughout TFTPClient class (now there's a shutdown option at every question the client is asked). Handled packet duplication testing.
Dominic: N/A
Adam: Created Sequence Diagram, and State Digrams worked at the TFTPClient interface with Johan.
Lyndon: Worked on State Diagrams and Use Cases.
Ahmad: Got the file transfer to work properly (it was not functional in our iteration 1).



Detailed set up and test instructions, including test files used:

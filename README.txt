# SYSC3303Project-Iteration 4

Adam Labelle 101038735
Johan Westeinde 101055222
Lyndon Lo 101030526
Ahmad Chaudhry 101003005



File Names and Setup:

Packet:

TFTPClient: This class is the client class where the user will choose what data to read/write. At any point, the user can enter 'quit' and the client will shutdown. Once we run the client, the user will be asked if they want to run the client in verbose mode (Y/N). Once they enter a valid response, they will be asked if they want to run in normal mode or test mode (Normal/Test). Once they enter a valid response, they will be asked if they will be running on the local computer (Y/N). All the options that the user enters will be saved and slightly alter the running of the Client, as the project outline explains.
Once they enter a valid response, they will be prompted to enter the request they wish to make a read request or a write request (RRQ or WRQ). From here there are two different paths to take. First, if the user enters RRQ, meaning they want to do a read request, they will be asked what file they want to access from the server. If they don't enter a valid file name (one that exists on the server side), they will be told they have entered an invalid file name and will be prompted to re-enter the name of the file they want to access. Once they enter a valid file name, they will be asked to enter the name of the file where they want to save it (on the client side). If they enter a file that already exists, they will be prompted to enter a different file name. The other option is that they enter WRQ, then they will be asked to enter the name of the file where they want to save to (on the server side). If they enter a file name that already exists, they will be prompted to enter a different file name. Once they enter a valid file name, they enter the name of the file they want to read (client side).

From here, the datagram packet that contains the RRQ or WRQ gets sent from a datagram socket and passed to the TFTPErrorSim, which then passes it on to the TFTPServer. The file transfer goes on to completion.

TFTPErrorSim: The error simulator, can handle lost, duplicate, delayed packets and error packets.

TFTPHelper: This is the class that deals with the received packets from port 69. The server creates a new helper thread on a random port and lets it handle the data transfer.

TFTPServer: This is the class that represents the Server. First, it's prompted on if we would like to run it in verbose mode (Y/N). Once we enter a valid option, a socket of type MasterSocket gets created on port 69 and waits to receive datagrams. When datagrams come in, they get passed on to ServerWorker threads to deal with the data transfer.



Breakdown of responsibilities of each team member for this and previous iterations:

Iteration 1:
When we submitted iteration 1 we didn't have the file transfer working properly.

Johan: Created ClientConnectionThread class, the sendAndReceive() method in the Client class, created the class diagram.
Dominic: Worked on the server handling & client connection threads, use cases.
Adam: Handled the shut down.
Lyndon: Created the scanners to prompt the user to enter the action they want to perform (read, write, quit) and the file name they wish to read/write to/from. Added multithreaded server.
Ahmad: Wasn't on our team for Iteration 1


Iteration 2:

Johan: Finalized the README from all group members collective information. Updated TFTPErrorSim class to ask for packet type and block number of the datagram packet that we want to test. Added client shutdown option throughout TFTPClient class (now there's a shutdown option at every question the client is asked). Handled packet duplication testing.
Dominic: N/A
Adam: Created Sequence Diagram, and State Digrams worked at the TFTPClient interface with Johan.
Lyndon: Worked on State Diagrams and Use Cases.
Ahmad: Got the file transfer to work properly (it was not functional in our iteration 1).


Iteration 3:

Johan: helped with server work, state machine diagrams, readme.
Adam: sequence diagram, worked on server work
Lyndon: use cases, worked on client work
Ahmad: the majority of the coding was done by Ahmad.

Iteration 4: 




Detailed set up and test instructions, including test files used:

Running the TFTP File Transfer
Step 1: Start up server, ErrorSim and then Client.
Step 2: Have a Client and Server Folder to be able to Read and Write Files into (already made in zip project).
Step 3: Set verbose to "y" on all three programs

To Read a File (steps performed under client program)
- Enter "Normal" (use Client to Server)
- Enter "Y" (will run on local computer)
- Enter "RRQ" (read request)
- Enter "512bytes.txt" (file is stored in server folder, server folder acts like server storage facility) 
- Enter filename to save as (can be anything .txt)
- new file will be saved under client folder with given name

To Write a File (steps performed under client program)
- Enter "Normal" (use Client to Server)
- Enter "Y" (will run on local computer)
- Enter "WRQ" (write request)
- Enter "512bytes.txt" (file is stored in client folder, client folder acts like client storage facility) 
- Enter filename to save as (can be anything .txt)
- new file will be saved under server folder with given name

To simulate error packet 1 (steps performed under client program)
- Enter "Normal" (use Client to Server)
- Enter "Y" (will run on local computer)
- Enter "RRQ" (write request)
- Enter "enrwionrewirwe.txt" or filename that does not exist on server 
- Enter filename to save as (can be anything .txt)
- file not found error will be received on client program

To simulate error packet 2 (steps performed under client program)
-first you need to change the permissions of a file
-navigate to the server folder, look for "servertestAV.txt" file and change its permissions to deny being read. 
- Enter "Normal" (use Client to Server)
- Enter "Y" (will run on local computer)
- Enter "RRQ" (write request)
- Enter "servertestAV.txt" (file is stored in server folder, server folder acts like server storage facility) 
- Enter filename to save as (can be anything .txt)
- access violation error will be received on client program

To simulate error packet 3 (steps performed under client program)
-under error program select choice 4 "Error Code 5: Change Port Number" for error option
- Enter Test" (use client to errorsim to server and viseversa)
- Enter "Y" (will run on local computer)
- Enter "RRQ" (write request)
- Enter "servertestAV.txt" (file is stored in server folder, server folder acts like server storage facility) 
- Enter filename to save as (can be anything .txt)
- error will be shown under server program, will timeout

To simulate error packet 6 (steps performed under client program)
- Enter "Normal" (use Client to Server)
- Enter "Y" (will run on local computer)
- Enter "RRQ" (write request)
- Enter filename to access, for example "servertest.txt" (can be anything file that exists in server folder)
- Enter "clienttest.txt" (file is stored in client folder, client folder acts like client storage facility) 
- file already exists error will be received on client program

To Quit Client and Server
- Enter 
- Enter Quit: Server

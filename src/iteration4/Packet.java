package iteration4;

/**
 * Packet.java
 * 
 * Iteration 2:
 * @author Ahmad Chaudhry
 * 
 * 
 * 
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Packet {
	// fixed values for use in code
	public static int PACKETSIZE = 512;
	public static int DATASIZE = 508;
	public static int MAXPACKETS = 65536;

	// define variables for use with packet
	private int Inquiry = 0;
	private byte[] BlockData = new byte[DATASIZE];
	private int packetNum = 0;
	private byte[] packetBytes = new byte[2]; // for packetNum
	private InetAddress Addr = null;
	private int Port = 0;
	private String FileName = "";
	private int ErrorCode = 0;
	private String ErrorMssg = null;
	private String mode = "netascii";
	private boolean valid;

	// getters for packet
	public int GetPacketNum() {
		return packetNum;
	}

	public int GetInquiry() {
		return Inquiry;
	}

	public byte[] GetData() {
		return BlockData;
	}
	
	public String GetMode() {
		return mode;
	}

	public int GetPort() {
		return Port;
	}

	public String GetFile() {
		return FileName;
	}

	public InetAddress GetAddress() {
		return Addr;
	}

	public int getErrorCode() {
		return ErrorCode;
	}

	public String getErrorMssg() {
		return ErrorMssg;
	}

	public boolean getValid() {
		return valid;
	}
	// setters for packet
	public void SetAddr(InetAddress Addr) {
		this.Addr = Addr;
	}

	public void setPort(int Port) {
		this.Port = Port;
	}

	// TFTP packet types can be found here https://tools.ietf.org/html/rfc1350

	// Send and Receive Packets
	public Packet(int Inquiry, String FileName) {
		this.Inquiry = Inquiry;
		this.FileName = FileName;
		valid = true;
	}

	// Data Packets
	public Packet(int Inquiry, byte[] blockData, int n) {
		this.Inquiry = Inquiry;
		this.BlockData = blockData;
		this.packetNum = n;
		valid = true;
	}

	// Ack Packets
	public Packet(int Inquiry, int n) {
		this.Inquiry = Inquiry;
		this.packetNum = n;
		valid = true;
	}

	// Error Packets
	public Packet(int Inquiry, int Ecode, String Emssg) {
		this.Inquiry = Inquiry;
		this.ErrorCode = Ecode;
		this.ErrorMssg = Emssg;
		valid = true;
	}

	// worker Packet
	public Packet(byte[] data) {
		valid = true;
		receiveBytes(data);
	}

	// Empty Packet
	public Packet() {

	}

	/**
	 * Method to find and return string of packet type
	 * 
	 * @return the packet type
	 */
	public String packetType() {

		if (Inquiry == 1) {
			return "RRQ";
		} else if (Inquiry == 2) {
			return "WRQ";
		} else if (Inquiry == 3) {
			return "DATA";
		} else if (Inquiry == 4) {
			return "ACK";
		} else if (Inquiry == 5) {
			return "ERROR";
		} else {
			return "Unknown";
		}

	}

	/**
	 * Method to convert packet data to bytes to send off in a datapacket
	 * 
	 * @return the data converted to bytes
	 * @throws IOException
	 */
	public byte[] convertBytes() throws IOException {
		ByteArrayOutputStream packet = new ByteArrayOutputStream();
		// if RRQ or WRQ
		if (Inquiry == 1 | Inquiry == 2) {
			packet.write(0); // first 0 byte in packet
			switch (Inquiry) { // make request all Lowercase to check
			case 1:
				packet.write(1); // write a 1 byte to the packet
				break;
			case 2:
				packet.write(2);// write a 2 byte to the packet
				break;
			}
			packet.write(FileName.getBytes()); // change name to bits and add to packet
			packet.write(0); // write a 0 to the packet
			packet.write(mode.getBytes()); // change mode to bits and add to packet
			packet.write(0); // write a 0 to the packet
			// if packet is a DATA packet
		} else if (Inquiry == 3) {
			int byteHold = 0;
			packet.write(0);
			packet.write(3);
			// if the current packet number is less than 256 write it to second byte for
			// BLOCK #
			if (packetNum < 256) {
				packet.write(byteHold);
				packet.write(packetNum);
				packetNum++;
				// if the current packet number is greater than 256 write it to first byte for
				// BLOCK # and leave second byte fixed at 255 (they will be added
				// to get the packetNum when they are decoded)
			} else if (packetNum > 255) {
				byteHold = 255;
				packetNum = 1;
				packet.write(packetNum);
				packet.write(byteHold);
				packetNum++;
				// if we have reached the max size of BLOCK #
			} else if (packetNum < 256 && byteHold == 255) {
				packet.write(255);
				packet.write(byteHold);
			}
			//for the length of the data, write it to the packet
			for (int j = 0; j < BlockData.length; j++) {
				packet.write(BlockData[j]);
			}
			//Case for ACK packet
		} else if (Inquiry == 4) {
			int byteHold = 0;
			packet.write(0);
			packet.write(4);
			//same concept as before for DATA packet
			if (packetNum < 256) {
				packet.write(byteHold);
				packet.write(packetNum);
				packetNum++;
			} else if (packetNum > 255) {
				byteHold = 255;
				packetNum = 1;
				packet.write(packetNum);
				packet.write(byteHold);
				packetNum++;
			} else if (packetNum < 256 && byteHold == 255) {
				packet.write(255);
				packet.write(byteHold);
			}
			//case for ERROR Packet 
		} else if (Inquiry == 5) {
			packet.write(0);
			packet.write(5);
			if (ErrorCode < 10) {
				packet.write(0);
				packet.write(ErrorCode);
			}
			packet.write(ErrorMssg.getBytes());
			packet.write(0);
		}
		valid = true;
		return packet.toByteArray();
	}
	/**
	 * method used to convert received bytes to values that can be stored in packet
	 * 
	 * @param data the bytes that need to be converted
	 * @return
	 */
	public boolean receiveBytes(byte[] data) {
		BlockData = data;
		int fileNamelength = 0;
		int ErrorMssglength = 0;
		int i = 0;
		// check if first bit is zero
		if (data[i] == 0) {
			// increment bit for inquiry byte
			i++;
			// check that inquiry byte falls between 1 and 5
			if (data[i] < 1 || data[i] > 5) {
				return false;
			}
			// inquiry is the current data bit
			Inquiry = data[i];
			// move to next bit
			// RRQ/WRQ Packet
			if (data[i] == 1 || data[i] == 2) {
				// check the length of the filename
				for (int j = i + 1; j < data.length; j++) {
					// check until 0 is found
					if (data[j] != 0) {
						fileNamelength++;
					} else {
						break;
					}
				}
				//create a holder for filenamebytes
				byte[] fileNameBytes = new byte[fileNamelength];
				for (int j = i + 1; j < data.length; j++) {
					// check until 0 is found
					if (data[j] != 0) {
						fileNameBytes[j - 2] = data[j];
					} else {
						break;
					}
				}
				// converts the bytes to a String for FileName
				FileName = new String(fileNameBytes);
				// Mode is default to netascii so no need to find
				valid = true;
				return true;
				// Data Packet
			} else if (data[i] == 3) {

				// convert first two bytes to an int for packetNum
				packetBytes[0] = data[i + 1];
				packetBytes[1] = data[i + 2];
				packetNum = ((packetBytes[0] & 0xff) << 8) | (packetBytes[1] & 0xff);
				// convert rest of bytes to data
				// keep track of blockData bytes
				int currByte = 0;
				// take remainder bytes and place in BlockData byte array
				for (int j = i + 3; j < data.length; j++) {
					BlockData[currByte] = data[j];
					currByte++;
				}
				valid = true;
				return true;
				// ACK packet
			} else if (data[i] == 4) {
				// convert first two bytes to an int for packetNum
				packetBytes[0] = data[i + 1];
				packetBytes[1] = data[i + 2];
				packetNum = ((packetBytes[0] & 0xff) << 8) | (packetBytes[1] & 0xff);
				valid = true;
				return true;
				// ERROR Packet
			} else if (data[i] == 5) {
				// convert first two bytes to an int for ErrorCode
				packetBytes[0] = data[i + 1];
				packetBytes[1] = data[i + 2];
				ErrorCode = ((packetBytes[0] & 0xff) << 8) | (packetBytes[1] & 0xff);
				// Convert Error message bytes to string
				// check the length of the error message
				for (int j = i + 3; j < data.length; j++) {
					// check until 0 is found
					if (data[j] != 0) {
						ErrorMssglength++;
					} else {
						break;
					}
				}
				//byte holder for error message
				byte[] ErrorMssgBytes = new byte[ErrorMssglength];
				for (int j = i + 3; j < data.length; j++) {
					// check until 0 is found
					if (data[j] != 0) {
						ErrorMssgBytes[j - 4] = data[j];
					} else {
						break;
					}
				}
				// converts the bytes to a String for FileName
				ErrorMssg = new String(ErrorMssgBytes);
				if (data[i + ErrorMssglength + 3] == 0) {
					valid = true;
					return true;
				} else {
					return false;
				}
			}

		} else {
			return false;
		}
		return false;
		// https://docs.oracle.com/javase/6/docs/api/java/util/Arrays.html#copyOfRange%28byte%5B%5D,%20int,%20int%29
	}

	/**
	 * Method to get the length of the blockdata for sending and receiving purposes
	 * 
	 * @return the length
	 */
	public int dataLength() {
		int length = 0;
		for (int j = 0; j < BlockData.length; j++) {
			// check until 0 is found
			if (BlockData[j] != 0) {
				length++;
			} else {
				break;
			}
		}
		return length;
	}

}
package iteration5;

/**
 * Packet.java
 * 
 * Iteration 4:
 * 
 * 
 * 
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Packet {
	// fixed values for use in code
	public static int PACKETSIZE = 516;
	public static int DATASIZE = 512;
	public static int MAXPACKETS = (int) Math.pow(2, 8 * 2);

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
	public byte[] convertBytes() {
		byte[] out = new byte[516];
		out[0] = 0;
		out[1] = (byte) Inquiry;
		int i = 2;
		if (Inquiry < 1 || Inquiry > 5)
			return null;
		if (Inquiry == 1 || Inquiry == 2) {
			i = offsetByteCopy(out, FileName.getBytes(), i);
			out[i++] = 0;
			i = offsetByteCopy(out, mode.getBytes(), i);
			out[i++] = 0;
			byte[] res = new byte[i];
			System.arraycopy(out, 0, res, 0, i);
			out = res;
		}
		if (Inquiry == 3) {
			i = offsetByteCopy(out, i2b(packetNum, 2), i);
			System.arraycopy(BlockData, 0, out, i, BlockData.length);
			i += BlockData.length;
			byte[] res = new byte[i];
			System.arraycopy(out, 0, res, 0, i);
			out = res;
		}
		if (Inquiry == 4) {
			i = offsetByteCopy(out, i2b(packetNum, 2), i);
			byte[] res = new byte[i];
			System.arraycopy(out, 0, res, 0, i);
			out = res;
		}
		if (Inquiry == 5) {
			i = offsetByteCopy(out, i2b(ErrorCode, 2), i);
			i = offsetByteCopy(out, ErrorMssg.getBytes(), i);
			out[i++] = 0;
			byte[] res = new byte[i];
			System.arraycopy(out, 0, res, 0, i);
			out = res;
		}
		return out;

	}

	private byte[] i2b(int integer, int size) {
		ByteBuffer dbuf = ByteBuffer.allocate(4);
		dbuf.putInt(integer);
		byte[] bytes = dbuf.array();
		byte[] result = new byte[size];
		for (int i = 0; (i < 4 && i < size); i++) {
			result[size - i - 1] = bytes[3 - i];
		}
		return result;
	}

	private int offsetByteCopy(byte[] out, byte[] bytes, int i) {
		if ((bytes.length + i) < out.length) {
			for (int k = 0; k < bytes.length; k++)
				out[k + i] = bytes[k];
			return i + bytes.length;
		}
		return -1;
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
				// create a holder for filenamebytes
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
				if (data.length > 516) {
					valid = false;
					return false;
				}
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
				// byte holder for error message
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
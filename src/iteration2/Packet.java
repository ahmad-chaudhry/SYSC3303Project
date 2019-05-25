package iteration2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class Packet {
	public static int PACKETSIZE = 512;
	public static int DATASIZE = 508;
	public static int MAXPACKETS = 65536;

	private int Inquiry = 0;
	private byte[] BlockData = new byte[DATASIZE];
	private int packetNum = 0;
	private InetAddress Addr = null;
	private int Port = 0;
	private String FileName = null;
	private int ErrorCode = 0;
	private String ErrorMssg = null;

	public int GetPacketN() {
		return packetNum;
	}

	public int GetInquiry() {
		return Inquiry;
	}

	public byte[] GetData() {
		return BlockData;
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
	}

	// Data Packets
	public Packet(int Inquiry, byte[] blockData, int n) {
		this.Inquiry = Inquiry;
		this.BlockData = blockData;
		this.packetNum = n;
	}

	// Ack Packets
	public Packet(int Inquiry, int n) {
		this.Inquiry = Inquiry;
		this.packetNum = n;
	}

	// Error Packets
	public Packet(int Inquiry, int Ecode, String Emssg) {
		this.Inquiry = Inquiry;
		this.ErrorCode = Ecode;
		this.ErrorMssg = Emssg;
	}
	
	//Empty Packet
	public Packet() {
		
	}

	public byte[] convertBytes() throws IOException {
		ByteArrayOutputStream packet = new ByteArrayOutputStream();
		String mode = "netascii";
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
		} else if (Inquiry == 3) {
			packet.write(0);
			packet.write(3);
			if (this.packetNum < 99) {
				packet.write(packetNum);
				packetNum++;
			} else if (this.packetNum == 99) {
				packetNum = 1;
				packet.write(packetNum);
			}
			for(int j = 0; j<BlockData.length;j++){
				packet.write(BlockData[j]);
			}
		} else if (Inquiry == 4) {
			packet.write(0);
			packet.write(4);
			if (this.packetNum < 99) {
				packet.write(packetNum);
				packetNum++;
			} else if (this.packetNum == 99) {
				packetNum = 1;
				packet.write(packetNum);
			}
		} else if (Inquiry == 5) {
			packet.write(0);
			packet.write(5);
			packet.write(ErrorCode);
			packet.write(ErrorMssg.getBytes());
			packet.write(0);
		}
		return packet.toByteArray();
	}
	
	public void receiveBytes() {
		
		//IMPLEMENT METHOD TO GET OUT BYTES FOR USE 
		
		//https://docs.oracle.com/javase/6/docs/api/java/util/Arrays.html#copyOfRange%28byte%5B%5D,%20int,%20int%29
		
		
	}
}
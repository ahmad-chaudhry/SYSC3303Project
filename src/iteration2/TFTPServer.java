package iteration2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TFTPServer {

	public static void main(String[] args) {
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
			System.out.println("Invalid Mode! Select either 'Y'(Yes), 'N'(No)");
		}

		ServerMaster SM = null;
		try {
			SM = new ServerMaster(verbose, new java.io.File(".").getCanonicalPath() + "\\server\\");
		} catch (IOException e) {
			e.printStackTrace();
		}
		SM.start();

		while (true) {
			System.out.println("\nEnter 'Q' followed by 'Enter' to quit at any time.\n");
			String input = sc.nextLine();
			if (input.toUpperCase().equals("Q")) {
				break;
			}
		}
		SM.Stop();
		sc.close();
	}

}

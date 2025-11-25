package UDPServer;

import java.net.*;
import java.util.Arrays;

import codec.Codec;
import messages.Ping;
import messages.Pong;

public class GBNUDPServer {
	public static void main(String args[]) throws Exception {

		int port = 9876;
		byte[] receiveData = new byte[1024];
		byte[] sendData;
		Pong pong = null;
		Ping ping = null;
		Codec codec = new Codec();
		InetAddress clientIPAddress = null;
		int sourcePort = 0;
		DatagramSocket serverSocket = null;
		boolean firstTry = false;

		try {
			serverSocket = new DatagramSocket(port);
			System.out.println("ServerSocket established on port: " + serverSocket.getLocalPort());

		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			e.getStackTrace();
			System.err.print("ServerSocket could not be established");

		}

		while (true) {

			try {
				DatagramPacket receivePacket = new DatagramPacket(receiveData,
						receiveData.length);

				serverSocket.receive(receivePacket);
				System.out.println("Ping received!");
				System.out.println("Sender-adress: " + receivePacket.getAddress() + ":" + receivePacket.getPort());
				ping = (Ping) (codec.decode(Arrays.copyOfRange(receiveData, 0, receivePacket.getLength())));
				System.out.println("Ping decoded");
				System.out.println("FROM Client: " + ping.toString());

				try {
					clientIPAddress = receivePacket.getAddress();
					sourcePort = receivePacket.getPort();

					System.out.println("ClientIP & port found: " + clientIPAddress + ": " + sourcePort);

				} catch (Exception e) {
					System.err.println("Exception: " + e.getMessage());
					e.printStackTrace();
					System.err.println("No Client found");
					serverSocket.close();
					break;
				}

				pong = new Pong(ping.header().sequence());
				System.out.println("Pong created: " + pong);

				if (pong.ack() == 2 && firstTry == false) {
					Thread.sleep(6000);
					firstTry = true;
				}

				sendData = pong.data();
				System.out.println("Pong encoded");
				System.out.println("Length: " + sendData.length);

				try {
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, clientIPAddress, sourcePort);

					serverSocket.send(sendPacket);
					System.out.println("Pong sent");

				} catch (Exception e) {
					System.err.println("Exception: " + e.getMessage());
					e.printStackTrace();
					System.err.println("Pong could not be sent");
					serverSocket.close();
					break;
				}

			} catch (Exception e) {
				System.err.println("Exception: " + e.getMessage());
				e.printStackTrace();
				serverSocket.close();
				break;
			}
		}
	}
}

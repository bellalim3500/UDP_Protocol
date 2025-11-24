package UDPServer;

import java.net.*;
import java.util.Arrays;

import codec.Codec;
import messages.Ping;
import messages.Pong;

class UDPServer {
	public static void main(String args[]) throws Exception {

		int port = 9876;
		byte[] receiveData = new byte[1024];
		byte[] sendData;
		Pong pong = null;
		Ping ping = null;
		Codec codec = new Codec();
		ServerState state = ServerState.WAIT_FOR_0;
		InetAddress clientIPAddress = null;
		int sourcePort = 0;
		DatagramSocket serverSocket = null;

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

				switch (state) {
					case ServerState.WAIT_FOR_0:

						// Set Timeout to simulate TimeOutException
						// Thread.sleep(6000);
						if (ping.header().sequence() != 0) {
							throw new Exception("Wrong msgID (1 when needed 0)");

						}
						pong = new Pong(ping.header().sequence());
						System.out.println("Pong created: " + pong);

						sendData = pong.data();
						System.out.println("Pong encoded");
						System.out.println("Length: " + sendData.length);

						try {
							DatagramPacket sendPacket = new DatagramPacket(sendData,
									sendData.length, clientIPAddress, sourcePort);

							serverSocket.send(sendPacket);
							System.out.println("Pong sent");
							state = ServerState.WAIT_FOR_1;

						} catch (Exception e) {
							System.err.println("Exception: " + e.getMessage());
							e.printStackTrace();
							System.err.println("Pong could not be sent");
							serverSocket.close();
							break;
						}

						break;

					case ServerState.WAIT_FOR_1:
						if (ping.header().sequence() != 1) {
							throw new Exception("Wrong msgID (0 when needed 1)");
						}

						// simulate wrong Pong
						// pong = new PongMsg(
						// new MsgHeader(0, MsgType.PONG, 0, "1A", System.currentTimeMillis(),
						// serverSocket.getLocalPort(), port));
						pong = new Pong(ping.header().sequence());
						System.out.println("Pong created: " + pong);

						sendData = pong.data();
						System.out.println("Pong encoded");
						System.out.println("Length: " + sendData.length);

						try {
							DatagramPacket sendPacket = new DatagramPacket(sendData,
									sendData.length, clientIPAddress, sourcePort);

							serverSocket.send(sendPacket);
							System.out.print("Pong sent");
							state = ServerState.WAIT_FOR_0;

						} catch (Exception e) {
							System.err.println("Exception: " + e.getMessage());
							e.printStackTrace();
							System.err.println("Pong could not be sent");
							serverSocket.close();
							break;
						}

						break;

					default:
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

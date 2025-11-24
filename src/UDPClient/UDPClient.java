package UDPClient;

import java.net.*;
import java.nio.charset.StandardCharsets;
import codec.Codec;
import messages.MsgHeader;
import messages.MsgType;
import messages.Ping;
import messages.Pong;

class UDPClient {

	public static void main(String args[]) throws Exception {

		final int MAXRETRIES = 5;
		int retries1 = 0;
		int retries0 = 0;

		int rounds = 0;

		DatagramSocket clientSocket = null;
		Ping ping = null;
		Pong pong = null;
		byte[] sendData = null;
		byte[] receiveData = new byte[1024];
		InetAddress serverIPAddress = null;
		int port = 9876;
		ClientState state = ClientState.WAIT_FOR_CALL0;
		DatagramPacket sendPacket = null;
		DatagramPacket receivePacket;
		boolean running;
		boolean ackReceived0 = false;
		boolean ackReceived1 = false;
		Codec codec = new Codec();
		float receiveStart = 0;
		float receiveEnd = 0;

		// BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
		// System.in));

		try {
			clientSocket = new DatagramSocket();
			System.out.println("ClientSocket established on port: " + clientSocket.getLocalPort());
			clientSocket.setSoTimeout(5000); // 5000 ms = 5 Sekunden
			System.out.println("SocketTimeout set to 5 seconds");

			serverIPAddress = InetAddress.getByName("localhost");

			System.out.println("ServerIP found: " + serverIPAddress);
			running = true;
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
			System.err.println("Initialization failed");
			clientSocket.close();
			return;

		}

		while (running && rounds < 3) {
			try {

				switch (state) {
					case ClientState.WAIT_FOR_CALL0:
						retries0 = 0;
						ackReceived0 = false;

						ping = new Ping(new MsgHeader(0, 0, MsgType.PING, "12", "1A", System.currentTimeMillis()));
						System.out.println("Ping created: " + ping.toString() + ping.header().msgId());

						sendData = codec.encode(ping);
						System.out.println("Ping encoded");
						System.out.println("Length: " + sendData.length);

						sendPacket = new DatagramPacket(sendData,
								sendData.length, serverIPAddress, port);

						clientSocket.send(sendPacket);
						receiveStart = System.currentTimeMillis();
						System.out.println("Ping sent");
						state = ClientState.WAIT_FOR_ACK0;
						break;

					case ClientState.WAIT_FOR_ACK0:

						while (retries0 < MAXRETRIES && !ackReceived0) {

							try {

								receivePacket = new DatagramPacket(receiveData,
										receiveData.length);

								clientSocket.receive(receivePacket);
								receiveEnd = System.currentTimeMillis();

								System.out.println("Pong received!");
								System.out.println(
										"Sender-adress: " + receivePacket.getAddress() + ":" + receivePacket.getPort());

								pong = new Pong(Integer.parseInt(new String(receivePacket.getData(), 0,
										receivePacket.getLength(), StandardCharsets.UTF_8)));
								System.out.println("Pong decoded");
								System.out.println("FROM SERVER: " + pong);

								if (ping.header().sequence() == pong.ack()) {
									state = ClientState.WAIT_FOR_CALL1;
									System.out.println("RTT: " + calculateRTT(receiveStart, receiveEnd));
									rounds++;
									ackReceived0 = true;

								} else {
									throw new Exception("Wrong AckNo (1 when needed 0)");
								}
							} catch (SocketTimeoutException e) {
								retries0++;
								System.err.println("Timeout, Retry No: " + retries0);
								clientSocket.send(sendPacket);
							}
						}
						if (!ackReceived0) {
							System.err.println("Maximale Wiederholungen erreicht, Client wird beendet.");
							running = false;
						}

						break;
					case ClientState.WAIT_FOR_CALL1:

						retries1 = 0;
						ackReceived1 = false;

						ping = new Ping(new MsgHeader(0, 1, MsgType.PING, "12", "1A", System.currentTimeMillis()));
						System.out.println("Ping created: " + ping.toString() + ping.header().msgId());

						sendData = codec.encode(ping);
						System.out.println("Ping encoded");
						System.out.println("Length: " + sendData.length);

						sendPacket = new DatagramPacket(sendData,
								sendData.length, serverIPAddress, port);

						clientSocket.send(sendPacket);
						receiveStart = System.currentTimeMillis();
						System.out.println("Ping sent");
						state = ClientState.WAIT_FOR_ACK1;
						break;

					case ClientState.WAIT_FOR_ACK1:

						while (retries1 < MAXRETRIES && !ackReceived1) {

							try {

								receivePacket = new DatagramPacket(receiveData,
										receiveData.length);

								clientSocket.receive(receivePacket);
								System.out.println("Pong received!");
								System.out.println(
										"Sender-adress: " + receivePacket.getAddress() + ":" + receivePacket.getPort());
								pong = new Pong(Integer.parseInt(new String(receivePacket.getData(), 0,
										receivePacket.getLength(), StandardCharsets.UTF_8)));

								System.out.println("Pong decoded");
								System.out.println("FROM SERVER: " + pong);

								if (ping.header().sequence() == pong.ack()) {
									state = ClientState.WAIT_FOR_CALL0;
									System.out.println("RTT: " + calculateRTT(receiveStart, receiveEnd));
									rounds++;
									ackReceived1 = true;

								} else {
									throw new Exception("Wrong AckNo (1 when needed 0)");
								}
							} catch (SocketTimeoutException e) {
								retries1++;
								System.err.println("Timeout, Wiederholung Nr. " + retries1);
								clientSocket.send(sendPacket);
							}
						}

						if (!ackReceived1) {
							System.err.println("Maximale Wiederholungen erreicht, Client wird beendet.");
							running = false;
						}

					default:
						break;
				}
			} catch (Exception e) {
				System.err.println("Exception occurred:");
				e.printStackTrace();
				if (clientSocket != null && !clientSocket.isClosed()) {
					clientSocket.close();
					System.out.println("Client closed due to error.");
				}
				running = false;

			}

		}
		if (clientSocket != null && !clientSocket.isClosed())

		{
			clientSocket.close();
			System.out.println("Cient closed, client terminated.");
		}
		running = false;

	}

	public static float calculateRTT(float start, float end) {
		return start - end;
	}
}

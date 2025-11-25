package UDPClient;

import java.net.*;
import java.nio.charset.StandardCharsets;
import codec.Codec;
import messages.MsgHeader;
import messages.MsgType;
import messages.Ping;
import messages.Pong;

public class GBNClient {

    public static void main(String args[]) throws Exception {
        DatagramSocket clientSocket = null;
        Ping ping = null;
        Ping[] lastFivePings = new Ping[5];
        Pong pong = null;
        byte[] sendData = null;
        byte[] receiveData = new byte[1024];
        InetAddress serverIPAddress = null;
        int port = 9876;
        GBNClientState state = GBNClientState.WAIT_FOR_CALL;
        DatagramPacket sendPacket = null;
        DatagramPacket receivePacket;
        Codec codec = new Codec();
        int pipelineSequence = 0;
        float receiveStart = 0;
        float receiveEnd = 0;
        int j = 0;
        int failedAckIndex = -1;
        boolean running;
        boolean ackReceived = true;

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

        while (running) {
            try {
                switch (state) {
                    case GBNClientState.WAIT_FOR_CALL:
                        for (int i = 0; i < 5; i++) {
                            if (!ackReceived) {
                                ping = lastFivePings[j];
                                lastFivePings[i] = ping;
                                j++;
                                if (j % 5 == 0) {
                                    ackReceived = true;
                                }
                            } else {
                                ping = new Ping(new MsgHeader(0, pipelineSequence, MsgType.PING, "12", "1A",
                                    System.currentTimeMillis()));
                                lastFivePings[i] = ping;
                                pipelineSequence++;
                            }
                            
                            System.out.println("Ping created: " + ping.toString() + ping.header().msgId());

                            sendData = codec.encode(ping);
                            System.out.println("Ping encoded");
                            System.out.println("Length: " + sendData.length);

                            sendPacket = new DatagramPacket(sendData,
                                    sendData.length, serverIPAddress, port);

                            clientSocket.send(sendPacket);
                            receiveStart = System.currentTimeMillis();
                            System.out.println("Ping sent");
                        }
                        state = GBNClientState.WAIT_FOR_ACK;
                        break;
                    case GBNClientState.WAIT_FOR_ACK:
                        j = 0;
                        do {
                            try {
                                // recieves duplicates but doesn't process the data, -1 if everythings okay
                                while (failedAckIndex >= 0) {
                                    receivePacket = new DatagramPacket(receiveData,
                                        receiveData.length);

                                    clientSocket.receive(receivePacket);
                                    receiveEnd = System.currentTimeMillis();

                                    failedAckIndex--;
                                }

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
                                System.out.println("FROM SERVER: " + pong.ack());
                                
                                // if not all five pipelined packages are sent ArrayOutOfBoundsException
                                if (pong.ack() >= lastFivePings[j].header().sequence()) {
                                    j++;
                                    System.out.println("RTT: " + calculateRTT(receiveStart, receiveEnd));
                                    ackReceived = true;
                                } else {
                                    throw new Exception("Wrong AckNo");
                                }
                            } catch (SocketTimeoutException e) {
                                System.err.println("Timeout");
                                ackReceived = false;
                                state = GBNClientState.WAIT_FOR_CALL;
                                failedAckIndex = j;
                                break;
                            }
                        } while (ackReceived && j != 5);

                        state = GBNClientState.WAIT_FOR_CALL;
                        break;
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
    }

    public static float calculateRTT(float start, float end) {
        return end - start;
    }
}

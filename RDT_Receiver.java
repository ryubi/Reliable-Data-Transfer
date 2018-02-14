import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created by ryanubinger on 11/9/17.
 */
public class RDT_Receiver
{
    public static String displayCharValues(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append((int) c).append(",");
        }

        System.out.println(sb.toString());

        return sb.toString();
    }

    static int flipBit(int bit)
    {
        if (bit == 0)
            return 1;
        else
            return 0;
    }

    static String naughtyReceiver(String rcvPkt, int seqBit)
    {
        int eventNum, falseSeq;
        Random rand = new Random();

        eventNum = rand.nextInt(4) + 1;

        System.out.println("How do you respond?");
        System.out.println("1) send a correct ACK; 2) send a corrupted ACK; 3) do not send any ACK; 4) Send a wrong ACK");

        switch (eventNum)
        {
            case 1: rcvPkt = "ACK" + " " + seqBit + " "; // correct ACK
                    System.out.println("Receiver correctly responds with: " + rcvPkt);
                    break;
            case 2: rcvPkt = "Bad ACK "; // corrupted ACK for different checksum (9 denotes corruption)
                    System.out.println("A corrupted ACK is sent");
                    break;
            case 3: System.out.println("Receiver does not send an ACK"); // do nothing for No ACK
                    break;
            case 4: falseSeq = flipBit(seqBit); // sends incorrect ACK
                    rcvPkt = "ACK" + " " + falseSeq + " ";
                    System.out.println("Receiver incorrectly responds with: " + rcvPkt);
                    break;
        }

        return rcvPkt;
    }

    public static void main(String[] args) throws IOException
    {
        int port = 5556;
        int expectedSeq, seqBit;
        byte[] send = new byte[1000];
        byte[] rcv;
        String sendMessage, rcvMessage, prevMessage;
        DatagramPacket sndPacket, rcvPacket;

        DatagramSocket rcvSocket = new DatagramSocket(port);

        System.out.println("RDT Receiver:");

        // initialization
        expectedSeq = 0;
        rcvMessage = "";
        sndPacket = new DatagramPacket(send, send.length);

        // main functionality
        while (true)
        {
            rcvSocket.receive(sndPacket);

            send = sndPacket.getData();
            sendMessage = new String(send, Charset.forName("UTF-8"));

            StringTokenizer st = new StringTokenizer(sendMessage);
            prevMessage = st.nextToken();

            seqBit = Integer.parseInt(st.nextToken());

            if (expectedSeq != seqBit)
            {
                System.out.println("Receiver just correctly received a duplicate packet: " + prevMessage + " " + seqBit);

                rcvMessage = "ACK" + " " + seqBit + " ";
                rcv = rcvMessage.getBytes(Charset.forName("UTF-8"));
                rcvPacket = new DatagramPacket(rcv, rcv.length, InetAddress.getByName("127.0.0.1"), sndPacket.getPort());

                rcvSocket.send(rcvPacket);

                System.out.println("Receiver responds with " + rcvMessage);

            }
            else
            {
                System.out.println("Receiver just correctly received a packet: " + prevMessage + " " + seqBit);

                rcvMessage = naughtyReceiver(rcvMessage, seqBit);

                expectedSeq = flipBit(expectedSeq);

                if (rcvMessage.equals(""))
                    continue;

                rcv = rcvMessage.getBytes(Charset.forName("UTF-8"));
                rcvPacket = new DatagramPacket(rcv, rcv.length, InetAddress.getByName("127.0.0.1"), sndPacket.getPort());

                rcvSocket.send(rcvPacket);
            }
        }
    }
}

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

/**
 * Created by ryanubinger on 11/9/17.
 */
public class RDT_Sender
{
    static int flipBit(int bit)
    {
        if (bit == 0)
            return 1;
        else
            return 0;
    }

    public static void main(String[] args) throws IOException
    {
        int port = 5556;
        int seqBit, dataIndex;
        long startTime, currTime;
        byte[] sendPkt;
        byte[] rcvPkt = new byte[1000];
        String send, rcvMessage, temp;
        DatagramPacket sndPacket, rcvPacket;

        String[] data = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        System.out.println("RDT Sender:");

        DatagramSocket sendSocket = new DatagramSocket();

        System.out.println("Connected by: ('127.0.0.1', " + port + ")");

        // initialization
        sendSocket.setSoTimeout(200);
        rcvPacket = new DatagramPacket(rcvPkt, rcvPkt.length);
        seqBit = 0;
        dataIndex = 0;

        // main functionality
        while (true)
        {
            if (dataIndex == 7) // allows for continuous stream of data
                dataIndex = 0;

            send = data[dataIndex] + " " + seqBit + " ";
            sendPkt = send.getBytes(Charset.forName("UTF-8"));
            sndPacket = new DatagramPacket(sendPkt, sendPkt.length, InetAddress.getByName("127.0.0.1"), port);

            sendSocket.send(sndPacket);

            System.out.println("Sender sent a message: " + send);

            startTime = System.currentTimeMillis();

            while (true)
            {
                currTime = System.currentTimeMillis();
                if (currTime - startTime > 1000) {
                    System.out.println("Timeout. Send message again.");
                    break;
                }

                try
                {
                    sendSocket.receive(rcvPacket);
                }
                catch (SocketTimeoutException ste)
                {
                    System.out.println("Continue waiting.");
                    continue;
                }

                rcvPkt = rcvPacket.getData();
                rcvMessage = new String(rcvPkt, Charset.forName("UTF-8"));

                StringTokenizer st = new StringTokenizer(rcvMessage);
                temp = st.nextToken();

                if (temp.equals("ACK") && Integer.parseInt(st.nextToken()) == seqBit) // correct ACK
                {
                    System.out.println("Sender received a valid ACK " + seqBit + ", send next message.");
                    seqBit = flipBit(seqBit);
                    dataIndex++;
                    break;
                }
                else if (temp.equals("Bad")) // corrupted packet
                {
                    System.out.println("Sender received a corrupted ACK, keep waiting."); // do nothing because we are waiting for a packet with the correct checksum
                }
                else
                {
                    System.out.println("Sender received an ACK with the wrong sequence #, keep waiting."); // do nothing because we are waiting for a packet with the correct ACK
                }
            }
        }
    }
}

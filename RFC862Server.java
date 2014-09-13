import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RFC862Server{
    public static void main(String[] args) {
        DatagramSocket aSocket = null;
        try{
            aSocket = new DatagramSocket(7007);
            System.out.println("Listening at " + aSocket.getLocalAddress()
                    + " : " + aSocket.getLocalPort());
            byte[] buffer = new byte[1000];
            while(true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                System.out.println("Listening...");
                aSocket.receive(request);
                System.out.println("Received " + request.getLength()
                + " bytes from" + request.getAddress().toString()
                + " : " + request.getPort());
                DatagramPacket reply = new DatagramPacket(request.getData(),
                        request.getLength(), request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
        }catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }finally {
            if (aSocket != null) aSocket.close();
        }
    }
}
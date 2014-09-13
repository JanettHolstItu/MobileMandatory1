import java.net.*;
import java.io.*;

public class TCPForwarder {

    public static void main(String args[]) throws Exception {
        InetAddress i = InetAddress.getByName("106.185.40.123");
        forwarder(i, 7895, 7);
    }

    public static void forwarder(InetAddress i, int p1, int p2) throws Exception {
        ServerSocket listenSocket = null;
        try {
            listenSocket = new ServerSocket(p1);
            Socket sendSocket = new Socket(i, p2);
            while (true) {
                System.out.println("Listening on " + listenSocket.getInetAddress() + ":" + listenSocket.getLocalPort());
                Socket clientSocket = listenSocket.accept();
                System.out.println("Accepted connection");
                new Connection(clientSocket, sendSocket);
                Thread.sleep(8000);
            }
        } finally {
            if (listenSocket != null) {
                listenSocket.close();
            }

        }
    }
}

class Connection extends Thread {

    DataInputStream getSocket_in;
    DataOutputStream getSocket_out;
    DataInputStream sendSocket_in;
    DataOutputStream sendSocket_out;
    Socket recieveSocket;
    Socket sendSocket;

    public Connection(Socket getSocket, Socket sendSocket) throws Exception {
        this.sendSocket = sendSocket;
        this.recieveSocket = getSocket;
        sendSocket_in = new DataInputStream(this.sendSocket.getInputStream());
        sendSocket_out = new DataOutputStream(this.sendSocket.getOutputStream());
        getSocket_in = new DataInputStream(this.recieveSocket.getInputStream());
        getSocket_out = new DataOutputStream(this.recieveSocket.getOutputStream());
        this.start();
    }

    public void run() {
        try {
            String data = getSocket_in.readUTF();
            System.out.println("Recieved msg from " + recieveSocket.getInetAddress().toString() + ":" + recieveSocket.getPort());
            System.out.println("Message recieved: " + data);
            
            System.out.println("Forwarding message...");
            sendSocket_out.writeUTF("Forward: " + data);
            Thread.sleep(2000);
            System.out.println("Message is forwarded to " + sendSocket.getInetAddress() + ":" + sendSocket.getPort());
            String backData = sendSocket_in.readUTF();
            Thread.sleep(2000);
            System.out.println("Message recieved from forwarded host: " + backData);
            getSocket_out.writeUTF(backData);
            Thread.sleep(2000);
            System.out.println("All done!");
        } catch (Exception e) {
            System.out.println("Connection dead: " + e.getMessage());
        } finally {
            try {
                sendSocket.close();
            } catch (IOException e) {
                System.out.println("Close failed: " + e.getMessage());
            }
        }
    }
}

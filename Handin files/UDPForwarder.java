//Write a UDP forwarder. Your program must accept as input a hostname or ip h and two ports p1, p2. 
//Your program must then listen on the local host on p1, forwarding all datagrams received on p1 to host h 
//at port p2. Ignore return traffic from h.
//Submit your solution as a single Java-file UDPForwarder.java.


import java.net.*;
import java.io.*;
public class UDPForwarder{
	public static void main(String args[]){
		
		String host = "localHost";
		int p2 = 7001;
		
				
		DatagramSocket aSocket = null;
		try{
			InetAddress aHost = InetAddress.getByName(host);
			aSocket = new DatagramSocket(6789);
			byte[] buffer = new byte[1000];
			while(true){
			DatagramPacket request = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(request);
			
			DatagramPacket reply = new DatagramPacket(
					request.getData(), request.getLength(), aHost, p2);
			aSocket.send(reply);
			
			System.out.println("Receives from socket on port: " + request.getPort());
			System.out.println("Sends to socket on port: " + p2);
		}
		} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {System.out.println("IO: " + e.getMessage());
		} finally {if (aSocket != null) aSocket.close();}
	}
}
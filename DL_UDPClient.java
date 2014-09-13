//Dataloss Client

import java.net.*;
import java.util.ArrayList;
import java.io.*;
public class DL_UDPClient{
	static ArrayList<String> messages;

	public static void main(String args[]){
		
		
		
		messages = new ArrayList<String>();
		for (int i=0; i<1000; i++){
			String m = i+"#Message";
			messages.add(m);
		}
		
		// args give message contents and server hostname
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName("130.226.142.243");
			//InetAddress bHost = InetAddress.getByAddress("130.226.142.243", null);
			int serverPort = 6789;
			for (int i=0; i< messages.size(); i++){
				String myString = messages.get(i);
				byte [] m = myString.getBytes();
				DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
				aSocket.send(request);
//				try {
//				    Thread.sleep(10);                 //1000 milliseconds is one second.
//				} catch(InterruptedException ex) {
//				    Thread.currentThread().interrupt();
//				}
			}
			
		} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		} catch (IOException e){System.out.println("IO: " + e.getMessage());
		} finally { if(aSocket != null) aSocket.close();}
	}
}
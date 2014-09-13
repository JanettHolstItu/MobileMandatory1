
import java.net.*;
import java.util.Random;
import java.io.*;
public class RUDPClient{
	
	static int counter = 0;
	
	public static void main(String args[]) {
		
		
		String host = args[0];
		int serverPort = Integer.parseInt(args[1]);
		String myString = args[2];
		DatagramSocket aSocket = null;
		
		// Creates an ID for the message
		Random rand = new Random();
		int current_id = (rand.nextInt(999999));
		
		sendMessage(current_id, aSocket, myString, host, serverPort);
		
	}

	private synchronized static void sendMessage(int id, DatagramSocket aSocket, String myString, String host, int serverPort) {
		counter++;
		try {
			myString = id+"@@"+myString;
			aSocket = new DatagramSocket();
			
			
			byte [] m = myString.getBytes();
			InetAddress aHost = InetAddress.getByName(host);
			DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
			aSocket.send(request);
			
			aSocket.setSoTimeout(5000);
			
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			String reply_string = new String(reply.getData()) + '"';
			String id_string = ""+id;
			
			//In case the wrong id returns due to concurrency issues.
			if (!(reply_string.contains(id_string)) && (counter<=10)) {
				System.out.println("Resending message");
				sendMessage(id, aSocket, myString, host, serverPort);
			} 
			//It tries to send the message 10 times before it fails
			else if(counter>10) System.out.println("Failed to send message");
			
			else System.out.println("Safely sent message!");
			
		} catch (SocketException e){System.out.println("Socket: " + e.getMessage()); //NumberFormatException
		} catch (SocketTimeoutException e){
			System.out.println("Resending message");
			sendMessage(id, aSocket, myString, host, serverPort);
		} catch (IOException e){System.out.println("IO: " + e.getMessage());
		
		} finally { if(aSocket != null) aSocket.close();}
	}
}
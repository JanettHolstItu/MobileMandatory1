import java.net.*;
import java.util.ArrayList;
import java.io.*;
public class RUDPServer{
	
	
	public static void main(String args[]){
		
		String host = "localHost";
		
		MyCache cache = new MyCache();
				
		DatagramSocket aSocket = null;
		try{
			InetAddress aHost = InetAddress.getByName(host);
			aSocket = new DatagramSocket(6789);
			byte[] buffer = new byte[1000];
			while(true){
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String message = new String(request.getData());
				String id = message.split("@@")[0];
				
				//Only post the message if it has not arrived more than once,
				//I.e. check the cache of the last 20 messages received.
				if (!(cache.contains(id))){
					cache.add(id);
					byte [] m = id.getBytes();
					DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
					aSocket.send(reply);
					
					String theMessage = message.split("@@")[1];
					System.out.println(theMessage);
				}
				
		}
		} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {System.out.println("IO: " + e.getMessage());
		} finally {if (aSocket != null) aSocket.close();}
	}
	
	static class MyCache extends ArrayList<String>{

		//The cache can contain 20 IDs, so that the server can handle concurrent messages. 
		//E.g. if a message is resent after 5 seconds, 
		//other messages might have arrived in the meantime.
		
		public MyCache(){
			super();
		}

		@Override
		public boolean add(String arg1) {
			super.add(0, arg1);
			if(this.size()>20){
				this.remove(this.size()-1);
			}
			return false;
		}
		
		
		
	}
}
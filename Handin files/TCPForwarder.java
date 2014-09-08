
import java.net.*;
import java.io.*;
public class TCPForwarder {
	public static void main (String args[]) {
		try{
			int P1Port = 7896;
			
			InetAddress P2Address = InetAddress.getByName("106.185.40.123");
			int P2Port = 7;
			Socket itu = new Socket(P2Address, P2Port);
			
			ServerSocket listenSocket = new ServerSocket(P1Port);
			
			while(true) {
				Socket clientSocket = listenSocket.accept();
//				Socket receiverSocket = new Socket(IPAddress, receiverPort);
				System.out.println("Socket created");
				Connection c = new Connection(clientSocket, itu);
				System.out.println("Connection made");
				
				//Connection c2 = new Connection(itu, true);
			}
		} catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
		}
	}
	class Connection extends Thread {
		DataInputStream from_in;
		DataOutputStream from_out;
		DataInputStream to_in;
		DataOutputStream to_out;
		Socket from;
		Socket to;
		
		public Connection (Socket fromSocket, Socket toSocket) {
			from = fromSocket;
			to = toSocket;
			try {
				from = fromSocket;
				to = toSocket;
				from_in = new DataInputStream( from.getInputStream());
				from_out =new DataOutputStream( from.getOutputStream());
				to_in = new DataInputStream( to.getInputStream());
				to_out =new DataOutputStream( to.getOutputStream());
				System.out.println("Streams ready");
				System.out.println("From - Port: "+from.getPort() + ", Local port: "+ to.getLocalPort());
				System.out.println("To - Port: "+to.getPort() + ", Local port: "+ to.getLocalPort());
				this.start();
			} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
		}
		public void run(){
			try { 
				String data = from_in.readUTF();
				System.out.println("Initial message received: "+data);
				
				to_out.writeUTF(data); // UTF is a string encoding; see Sec 4.3
				System.out.println("Sent forwarded message");
				String data2 = to_in.readUTF();
				System.out.println("Received ecco of forwarded message: "+ data2);
				
				
			} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
			} catch(IOException e) {System.out.println("IO:"+e.getMessage());
			} finally { try {
				from.close();
				to.close();
				}catch (IOException e){/*close failed*/}
			
		}
	}
}
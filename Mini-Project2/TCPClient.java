//package MiniProject2;

import java.net.*;
import java.io.*;
public class TCPClient {
	
	static int port;
	
	public static void main (String args[]) {
		// arguments supply message and hostname of destination
		port = 0;
		
		enterSink();
	}

	private static void enterSink() {
		Socket s = null;
		try{
			int serverPort = 7896;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			out.writeUTF("enterSink"); // UTF is a string encoding; see Sec 4.3
			String data = in.readUTF();
			port = Integer.parseInt(data);
			System.out.println("Port: "+ port);
		}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
		} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
		} catch (IOException e){System.out.println("IO:"+e.getMessage());
		} finally {if(s!=null) try {
			s.close();}catch (IOException e){/*close failed*/}}
		
		startListening(port);
	}

	private static void startListening(int p) {
		final int port = p;
		System.out.println("started listening with port "+port);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					
					ServerSocket listenSocket = new ServerSocket(port);
					while(true) {
						Socket clientSocket = listenSocket.accept();
						MakeConnection c = new MakeConnection(clientSocket);
					}
				} catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
			}
		});
		
		t.start();
	}
}

class MakeConnection extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	
	public MakeConnection (Socket aClientSocket) {
		try {
			clientSocket = aClientSocket;
			in = new DataInputStream( clientSocket.getInputStream());
			out =new DataOutputStream( clientSocket.getOutputStream());
			this.start();
		} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
	}
	public void run(){
		try { // an echo server
			String data = in.readUTF();
			System.out.println(data);
			
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}
		}
	}
}
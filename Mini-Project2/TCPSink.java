import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
public class TCPSink {
	
	static int port;
	
	public static void main (String args[]) {
		// arguments supply message and hostname of destination
		System.out.println("Use keywords 'Enter' and 'Exit'.");
		Scanner sc = new Scanner(System.in);
		
		port = 0;
		Thread listening = new Thread();
        
        while(sc.hasNextLine()) {
        	String next = sc.nextLine();
        	if (next.equalsIgnoreCase("Enter")){
        		listening = enterSink();
        	} else if (next.equalsIgnoreCase("Exit")){
        		exitSink(listening);
        		
        	}
        }
		
		
		
	}

	private static void exitSink(Thread listening) {
		listening.interrupt();
		
		Socket s = null;
		try{
			int serverPort = 7896;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			out.writeUTF("exitSink"); // UTF is a string encoding; see Sec 4.3
			out.writeUTF(String.valueOf(port)); // UTF is a string encoding; see Sec 4.3
		} catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
		} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
		} catch (IOException e){System.out.println("IO:"+e.getMessage());
		} finally {if(s!=null) try {
			s.close();}catch (IOException e){/*close failed*/}}
	}

	private static Thread enterSink() {
		Socket s = null;
		try{
			int serverPort = 7896;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			out.writeUTF("enterSink"); // UTF is a string encoding; see Sec 4.3
			String data = in.readUTF();
			port = Integer.parseInt(data);
		}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
		} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
		} catch (IOException e){System.out.println("IO:"+e.getMessage());
		} finally {if(s!=null) try {
			s.close();}catch (IOException e){/*close failed*/}}
		
		Thread listeningThread = startListening(port);
		return listeningThread;
	}

	private static Thread startListening(int p) {
		final int port = p;
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					
					ServerSocket listenSocket = new ServerSocket(port);
					while(true) {
						Socket clientSocket = listenSocket.accept();
						MakeConnection c = new MakeConnection(clientSocket);
					}
				} 
				catch(ClosedByInterruptException e){
					System.out.println("Stopped Listening.");
				}
				catch(IOException e) {
					System.out.println("Listen :"+e.getMessage());
				}
			}
		});
		
		t.start();
		return t;
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
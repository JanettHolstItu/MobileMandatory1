//package MiniProject2;

import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
public class TCPServer {
	
	static SyncedArray sinks;
	
	public static void main (String args[]) {
		
		sinks = new SyncedArray();
		
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				try{
					int serverPort = 7896;
					ServerSocket listenSocket = new ServerSocket(serverPort);
					while(true) {
						Socket clientSocket = listenSocket.accept();
						ListenForConnections c = new ListenForConnections(clientSocket, sinks);
					}
				} catch(IOException e) {
					//Do nothing. It catches this exception every time a source writes.
					//Optimally, it should be handled differently. 
					//However, we chose to make the thread catch it, to make it simple.
				}
			}
		});
		
		t.start();
		
		Scanner sc = new Scanner(System.in);
        System.out.println("This is the centered Server forwarding messages from clients to sources.");
        while(sc.hasNextLine()) {
        	String next = sc.nextLine();
        	ArrayList<String> array = sinks.getAll();
        	sendToAllSinks(array, next);
        	
        }
	}

	public static void sendToAllSinks(ArrayList<String> array, String next) {
		for (String s: array){
    		final int currentPort = Integer.parseInt(s);
			Socket socket;
			try {
				socket = new Socket("localhost", currentPort);
				SendMessage s1 = new SendMessage(socket, next);
				System.out.println("Sent message to "+ currentPort);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	}
	}
}
class SendMessage extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket socket;
	String message;
	
	
	public SendMessage (Socket aClientSocket, String m) {
		try {
			message = m;
			socket = aClientSocket;
			in = new DataInputStream( socket.getInputStream());
			out =new DataOutputStream( socket.getOutputStream());
			this.start();
			
		} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
	}
	public void run(){
		Socket socket = null;
		try{
    		out.writeUTF(message); // UTF is a string encoding; see Sec 4.3
		}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
		} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
		} catch (IOException e){System.out.println("IO:"+e.getMessage());
		} finally {if(socket!=null) try {socket.close();}catch (IOException e){/*close failed*/}}
	}
}

class ListenForConnections extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	SyncedArray myS;
	
	public ListenForConnections (Socket aClientSocket, SyncedArray sink) {
		try {
			myS = sink;
			clientSocket = aClientSocket;
			in = new DataInputStream( clientSocket.getInputStream());
			out =new DataOutputStream( clientSocket.getOutputStream());
			this.start();
		} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
	}
	public void run(){
		try { // an echo server
			String data = in.readUTF();
			ArrayList<String> arraySinks = myS.getAll();
			switch(data){
			case("enterSink"):
				myS.add(String.valueOf(clientSocket.getPort()));
				out.writeUTF("" + clientSocket.getPort());
	        	TCPServer.sendToAllSinks(arraySinks, "Sink entered on port "+ clientSocket.getPort());
				break;
			case("exitSink"):
				String currentPort = in.readUTF();
				myS.remove(currentPort);
	        	TCPServer.sendToAllSinks(arraySinks, "Sink exited from port "+ currentPort);
				break;
			case("enterSrc"):
//				mySrc.add(String.valueOf(clientSocket.getPort()));
				TCPServer.sendToAllSinks(arraySinks, "Source entered on port "+ clientSocket.getPort());
				while(true){
					int currentPort2 = clientSocket.getPort();
					String message = in.readUTF();
					if (message.equalsIgnoreCase("exit")) {
						TCPServer.sendToAllSinks(myS.getAll(), "Source exited from port "+ currentPort2);
						break;
					}
					TCPServer.sendToAllSinks(myS.getAll(), "Source ("+currentPort2+") says: "+ message);
				}
				break;
			default:
				break;
			}
			//out.writeUTF(data);
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}
		}
	}
	
}


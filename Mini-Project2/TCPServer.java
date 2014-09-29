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
						ListForConnections c = new ListForConnections(clientSocket, sinks);
					}
				} catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
			}
		});
		
		t.start();
		
		Scanner sc = new Scanner(System.in);
        System.out.println("Printing the file passed in:");
        while(sc.hasNextLine()) {
        	String next = sc.nextLine();
        	ArrayList<String> array = sinks.getAll();
        	sendToAllSinks(array, next);
        	
        }
		
		
//		try{
//			int serverPort = 7896;
//			ServerSocket listenSocket = new ServerSocket(serverPort);
//			while(true) {
//				Socket clientSocket = listenSocket.accept();
//				Connection c = new Connection(clientSocket);
//			}
//		} catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
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

class ListForConnections extends Thread {
	DataInputStream in;
	DataOutputStream out;
	Socket clientSocket;
	SyncedArray myS;
	
	public ListForConnections (Socket aClientSocket, SyncedArray sink) {
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
			System.out.println("Data: "+ data);
			System.out.println("port: "+clientSocket.getPort());
			ArrayList<String> array = myS.getAll();
			switch(data){
			case("enterSink"):
				myS.add(String.valueOf(clientSocket.getPort()));
				out.writeUTF("" + clientSocket.getPort());
	        	TCPServer.sendToAllSinks(array, "Sink entered on port "+ clientSocket.getPort());
				break;
			case("exitSink"):
				String currentPort = in.readUTF();
				myS.remove(currentPort);
	        	TCPServer.sendToAllSinks(array, "Sink exited from port "+ currentPort);
				break;
			case("enterSource"):
				break;
			case("exitSource"):
				break;
			}
			//out.writeUTF(data);
		} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
		} catch(IOException e) {System.out.println("IO:"+e.getMessage());
		} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}
		}
	}
}
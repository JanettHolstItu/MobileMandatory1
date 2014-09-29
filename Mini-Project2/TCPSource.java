//package MiniProject2;

import java.net.*;
import java.util.Scanner;
import java.io.*;
public class TCPSource {
	
	static int port;
	
	public static void main (String args[]) {
		// arguments supply message and hostname of destination
		System.out.println("Use keywords 'Enter' and 'Exit'.");
		Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine()) {
        	String next = sc.nextLine();
        	if (next.equalsIgnoreCase("Enter")){
        		enterSource();
        	} 
        }
	}
	private static void enterSource() {
		Socket s = null;
		try{
			int serverPort = 7896;
			s = new Socket("localhost", serverPort);
			DataInputStream in = new DataInputStream( s.getInputStream());
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			out.writeUTF("enterSrc"); // UTF is a string encoding; see Sec 4.3
			
			Scanner sc = new Scanner(System.in);
	        while(sc.hasNextLine()) {
	        	String next = sc.nextLine();
	        	out.writeUTF(next);
	        	if (next.equalsIgnoreCase("exit")){
	        		break;
	        	}
	        }
			
		}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
		} catch (EOFException e){System.out.println("EnterSource EOF:"+e.getMessage());
		} catch (IOException e){System.out.println("IO:"+e.getMessage());
		} finally {if(s!=null) try {
			s.close();}catch (IOException e){/*close failed*/}}
		
	}

}

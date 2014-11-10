import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class PutClient {
	
	public PutClient(int nextPort, int key, String value){
		//Send to next peer. Only initiated from a Node.
		SendMessage2 m = new SendMessage2(nextPort, key, value);
	}
	
	public static void main(String[] args){
		
		Scanner sc = new Scanner(System.in);
		while (sc.hasNext()){
			String input = sc.nextLine();
			
			if(input.length()>2 && input.substring(0, 3).equalsIgnoreCase("PUT")){
				String[] strings = input.split(" ");
				try{
					int port = Integer.parseInt(strings[1]);
					int key = Integer.parseInt(strings[2]);
					
					String value = "";
					String appos = "" + '"';
					if (strings.length <= 4){
						value = strings[3];
						if (value.contains("'")) value = value.replace("'", "");
						if (value.contains(appos)) value = value.replace(appos, "");
						SendMessage m = new SendMessage(port, key, value);
						m.join();
						System.exit(0);
					}
					else if (input.contains(appos)){
						value = input.split(appos)[1];
						SendMessage m = new SendMessage(port, key, value);
						m.join();
						System.exit(0);
					}
					else if (input.contains("'")) {
						value = input.split("'")[1];
						SendMessage m = new SendMessage(port, key, value);
						m.join();
						System.exit(0);
					}
					else
						System.out.println("you need a pair of "+appos + " around your message");
					
				} catch(NumberFormatException e){
					System.out.println("Your key and your port number needs to be Integers");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
			}
			else{
				System.out.println("You need to write: 'PUT [port] [yourKey] [yourValue]', e.g.: PUT 1025 1 " + '"' + "hello world" + '"');
			}
		}
	}
	
	static class SendMessage extends Thread {
		String message;
		Socket s = null;
		int serverPort;
		DataInputStream in;
		DataOutputStream out;
		
		public SendMessage(int port, int key, String value){
			this.message = "PUT("+key+","+value+")";
			this.serverPort = port;
			
			this.start();
		}
		
		public void run(){
			
			try{
				this.s = new Socket("localhost", serverPort);
				this.in = new DataInputStream( s.getInputStream());
				this.out = new DataOutputStream( s.getOutputStream());
				out.writeUTF(message); // UTF is a string encoding; see Sec 4.3
				//String data = in.readUTF();
				System.out.println("Send message: "+message);
				}catch (UnknownHostException e){
				System.out.println("Sock:"+e.getMessage());
				} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
				} catch (IOException e){System.out.println("IO:"+e.getMessage());
				} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}
			}
			
		}
		
	}
	
	static class SendMessage2 extends Thread {
		String message;
		Socket s = null;
		int serverPort;
		DataInputStream in;
		DataOutputStream out;
		
		public SendMessage2(int port, int key, String value){
			this.message = "PUTNEXT("+key+","+value+")";
			this.serverPort = port;
			
			this.start();
		}
		
		public void run(){
			
			try{
				this.s = new Socket("localhost", serverPort);
				this.in = new DataInputStream( s.getInputStream());
				this.out = new DataOutputStream( s.getOutputStream());
				out.writeUTF(message); // UTF is a string encoding; see Sec 4.3
				//String data = in.readUTF();
				System.out.println("Send message: "+message);
				}catch (UnknownHostException e){
				System.out.println("Sock:"+e.getMessage());
				} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
				} catch (IOException e){System.out.println("IO:"+e.getMessage());
				} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}
			}
			
		}
		
	}
}

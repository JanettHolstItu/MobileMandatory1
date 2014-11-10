import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GetClient {
	private int key;
	private int port;
	private int firstPort;
	ExecutorService executorService;
	Future<String> future;
	
	public static void main(String[] args){
		Scanner sc = new Scanner(System.in);
		while (sc.hasNext()){
			final String input = sc.nextLine();
			
			Thread thread = new Thread(){
			    public void run(){
			    	GetClient c = new GetClient(input);
			    }
			  };
			  thread.start();
			
		}
		
	}

	//port, ip, key
	public GetClient(String input){
		//GET 1025 1
		if(input.length()>8 && input.substring(0, 3).equalsIgnoreCase("GET")){
			String[] strings = input.split(" ");
			try{
				this.port = Integer.parseInt(strings[1]);
				this.key = Integer.parseInt(strings[2]);
				
				ExecutorService executorService = Executors.newSingleThreadExecutor();
				String message = "GET("+key+")";
				SendMessageAndListen s = new SendMessageAndListen(port, message);
				Future<String> future = executorService.submit(s);
				try {
				    // Real life code should define the timeout as a constant or
				    // retrieve it from configuration
				    String result = future.get(5, TimeUnit.SECONDS);
				    if (result != null)
				    	System.out.println("Found value: "+ result);
				    // Do something with the result
				} catch (TimeoutException e) {
				    future.cancel(true);
				    System.out.println("...Timed out.");
				    // Perform other error handling, e.g. logging, throwing an exception
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//send GET(key)
				
			} catch(NumberFormatException e){
				System.out.println("Your key and your port number needs to be Integers");
			} 
			
		}
		else{
			System.out.println("You need to write: 'GET [port] [yourKey]', e.g.: GET 1025 1");
		}
		
		
	}
	
	
	static class SendMessageAndListen implements Callable<String>{
		int port;
		String message;
		
		public SendMessageAndListen(int p, String m) {
			this.port = p;
			this.message = m;
		}

		@Override
		public String call() throws Exception {
			String value = "";
			
			Socket s = null;
			
			try{
				int serverPort = port;
				s = new Socket("localhost", serverPort);
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out = new DataOutputStream( s.getOutputStream());
				out.writeUTF(message); // UTF is a string encoding; see Sec 4.3
				value = in.readUTF();
				return value;
			}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
			} catch (EOFException e){System.out.println("Found no value");
			} catch (IOException e){System.out.println("IO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
			
			return null;
		}
		
	}
	
}



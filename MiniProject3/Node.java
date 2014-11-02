//package MiniProject3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicIntegerArray;


public class Node {
	static HeartBeatThread heartBeat;
	ConcurrentHashMap<Integer, String> map;
	//Array of (left, leftLeft, right, rightRight)
	AtomicIntegerArray friends;
	int myPort;
	
	public Node(int port){
		this.myPort = port;
		this.map = new ConcurrentHashMap<Integer, String>();
		//Array of (left, leftLeft, right, rightRight)
		friends = new AtomicIntegerArray(3);
		friends.set(0, 0);
		friends.set(1, 0);
		friends.set(2, 0);
		System.out.println(friends.toString());
	}
	
	public Node(int port, int nextNode){
		this.myPort = port;
		this.map = new ConcurrentHashMap<Integer, String>();
		//Array of (left, leftLeft, right)
		friends = new AtomicIntegerArray(3);
		friends.set(0, nextNode);
		friends.set(1, 0);
		friends.set(2, 0);
		System.out.println(friends.toString());
		
		
		///////////////////////////////////////////////////////////////////////////////
		//Update routing tables of Left, Right, and LeftLeft
		//Requires to send a getNeighbours() request to O(3) nodes
		//and a updateNeighbour() message to O(3) nodes.
		// --> Maximum 6 messages back and forth to create a new node
		
		ArrayList<Integer> left = getNeighBours(friends.get(0));
		ArrayList<Integer> right = null;
		System.out.println("left was: "+ left.toString());
		
		friends.set(1, left.get(0));
		
		if (left.get(2) == 0){ 								//N.left.right = null
			left.set(2, myPort);
			
		}
		else {
			int temp = left.get(2);
			left.set(2, myPort);
			friends.set(2, temp);
			right = getNeighBours(temp);
			System.out.println("right was: "+ right.toString());
			right.set(0, myPort);
			//Update right's left's left: My left.
			right.set(1, friends.get(0));
			//update right's table
			informNeighbour(friends.get(2), right);
			System.out.println("now right is: "+ right.toString());
			//update my rightRight's leftLeft:
			ArrayList<Integer> rightRight = getNeighBours(right.get(2));
			rightRight.set(1, myPort);
			informNeighbour(right.get(2), rightRight);
		}
		
		if (left.get(0) == 0){								//N.left.left = null

			friends.set(1, myPort);
			left.set(0, myPort);
			left.set(1, nextNode);
			int temp = friends.get(0);
			friends.set(2, temp);
		}
		
		//Update left's leftLeft:
		if (friends.get(1)!=myPort){
			ArrayList<Integer> leftLeft = getNeighBours(friends.get(1));
			int leftLeftLeft = leftLeft.get(0);
			left.set(1, leftLeftLeft);
		}
		
		System.out.println("now left is: "+ left.toString());
		System.out.println("and I am: "+ friends.toString());
		//Update left's routing table 
		informNeighbour(friends.get(0), left);
		
	}

	public static void main(String[] args){
		Node N = null;
		int p = 0;
		int n = 0;
		try{
			p = Integer.parseInt(args[0]);
		} catch(NumberFormatException e){
			System.out.println("Input an Integer as port");
			System.exit(0);
		}
		
		if (args.length>1)
			try{
				n = Integer.parseInt(args[1]);
				N = new Node(p, n);
			} catch(NumberFormatException e){
				System.out.println("Input Integers as ports");
				System.exit(0);
			}
		else N = new Node(p);
		
		/////////////////////////////////////////////////////////////////////////////
		//For security, I need to send all my values to the left. Therefore,
		//I now have to require might right to send me his values.
		//Takes O(m) messages, where m=number of total values.
		if(N.friends.get(0)!=0)
			getDublicates(N.friends.get(2));
		
		heartBeat = new HeartBeatThread(N);			
		
		try{
			int serverPort = N.myPort;
			ServerSocket listenSocket = new ServerSocket(serverPort);
			while(true) {
				Socket clientSocket = listenSocket.accept();
				Connection c = new Connection(clientSocket, N);
			}
		} catch(IOException e) {System.out.println("Listen :"+e.getMessage());}
	}
	
	private ArrayList<Integer> getNeighBours(int neighbour) {
		ArrayList<Integer> list = new ArrayList<Integer>(3);
		
		try {
			RequireNeighbours r = new RequireNeighbours(neighbour);
			
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Future<String> future = executorService.submit(r);
			String result = future.get(5, TimeUnit.SECONDS);
			String resultSplit[] = result.split(",");
			for (String s: resultSplit){
				int temp = Integer.parseInt(s);
				list.add(temp);
			}
			return list;
			//System.out.println(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void getDublicates(int neighbour) {
		ArrayList<Integer> list = new ArrayList<Integer>(3);
		
		try {
			RequireDublicates r = new RequireDublicates(neighbour);
			
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Future<String> future = executorService.submit(r);
			String result = future.get(5, TimeUnit.SECONDS);
			
			//System.out.println(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
	}
	
	private void informNeighbour(int neighbour, ArrayList<Integer> A){
		String message = String.valueOf(A.get(0));
		
		for (int j = 1; j < A.size(); j++) {
			message += ","+A.get(j);
		}
		try {
			InformNeighbours r = new InformNeighbours(neighbour, message);
			
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			Future<String> future = executorService.submit(r);
			String result = future.get(5, TimeUnit.SECONDS);
			
			System.out.println(neighbour + " was informed of update");
			//System.out.println(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
	}
	
	public void reRoute() {
		//Set my new left
		int next = friends.get(1);
		friends.set(0, next);
		
		//Set new leftLeft
		ArrayList<Integer> newLeft = getNeighBours(next);
		friends.set(1, newLeft.get(0));
		
		//update my new left's right to be me
		newLeft.set(2, myPort);
		informNeighbour(friends.get(0), newLeft);
		
		System.out.println("now left is: "+ newLeft.toString());
		System.out.println("and I am: "+ friends.toString());
		
		//Send dublicates to my new left
		for (Entry<Integer, String> entry : map.entrySet()) {
		    int _key = entry.getKey();
		    String _value = entry.getValue();
		    PutClient m = new PutClient(next, _key, _value);
		}
		
		//Start the new heartbeat:
		heartBeat = new HeartBeatThread(this);
		
	}
		
	static class Connection extends Thread {
		DataInputStream in;
		DataOutputStream out;
		Socket clientSocket;
		Node N;
		ConcurrentHashMap<Integer, String> map;
		
		public Connection (Socket aClientSocket, Node N2) {
			try {
				N = N2;
				clientSocket = aClientSocket;
				map = N.map;
				in = new DataInputStream( clientSocket.getInputStream());
				out =new DataOutputStream( clientSocket.getOutputStream());
				this.start();
			} catch(IOException e) {System.out.println("Connection:"+e.getMessage());}
		}
		public void run(){
			try { // an echo server
				String data = in.readUTF();
				String s = actOnInput(N, data);
				if (s != null)
				out.writeUTF(s);
			} catch(EOFException e) {System.out.println("EOF:"+e.getMessage());
			} catch(IOException e) {System.out.println("IO:"+e.getMessage());
			} finally { try {clientSocket.close();}catch (IOException e){/*close failed*/}
			}
		}
		
		public String actOnInput(Node N, String i){

			String input = new String(i);
			String value = null;
			
			if (input.equalsIgnoreCase("exit")) System.exit(0);
			
			else if (input.contains("HEARTBEAT")){
				return "<3";
			}
			
			else if (input.contains("PUTNEXT")){
				String firstSplit = input.split("\\(")[1];
				String secondSplit = firstSplit.split("\\)")[0];
				int key = Integer.parseInt(secondSplit.split(",")[0]);
				value = secondSplit.split(",")[1];
				map.put(key, value);
				System.out.println("You just Put value "+value +" at key "+key);
			}
			
			else if (input.substring(0, 3).equals("PUT")){
				String firstSplit = input.split("\\(")[1];
				String secondSplit = firstSplit.split("\\)")[0];
				int key = Integer.parseInt(secondSplit.split(",")[0]);
				value = secondSplit.split(",")[1];
				map.put(key, value);
				System.out.println("You just Put value "+value +" at key "+key);
				PutClient m = new PutClient(N.friends.get(0), key, value);
			}
			
			else if (input.equalsIgnoreCase("GETNEIGHBOURS")){
			
				String message = String.valueOf(N.friends.get(0));
				
				for (int j = 1; j < N.friends.length(); j++) {
					message += ","+N.friends.get(j);
				}
				return message;
			}
			
			else if (input.equalsIgnoreCase("GETDUBLICATES")){
				//For each element in my Hash, send a PUT to the requester.
				
				for (Entry<Integer, String> entry : map.entrySet()) {
				    int _key = entry.getKey();
				    String _value = entry.getValue();
				    PutClient m = new PutClient(N.friends.get(0), _key, _value);
				}
				
				return "true";
			}
			
			else if (input.contains("UPDATE")){
				String input2 = input.split(":")[1];
				String resultSplit[] = input2.split(",");
				for (int i1 = 0; i1<3; i1++){
					Integer temp = Integer.parseInt(resultSplit[i1]);
					N.friends.set(i1, temp);
				}
				System.out.println("New neighbours: "+N.friends.toString());
				heartBeat.update(N);
				return "true";
			}
			
			else if (input.substring(0, 3).equals("GET") && input.contains(",")){
				String firstSplit = input.split("\\(")[1];
				String secondSplit = firstSplit.split("\\)")[0];
				int key = Integer.parseInt(secondSplit.split(",")[0]);
				String firstPort = secondSplit.split(",")[1];
				int f_port = Integer.parseInt(firstPort);
				
				if (map.containsKey(key)){
					value = map.get(key);
					System.out.println("You got value '" +value+ "' from key "+key +" knowing first port "+firstPort);
				}
				else if(f_port != N.myPort){
					System.out.println("Looking for key...");
					ExecutorService executorService = Executors.newSingleThreadExecutor();
					String message = "GET("+key+","+firstPort+")";
					GetClient.SendMessageAndListen s = new GetClient.SendMessageAndListen(N.friends.get(0), message);
					Future<String> future = executorService.submit(s);
					try {
					    String result = future.get(5, TimeUnit.SECONDS);
					    return result;
					} catch (TimeoutException e) {
					    future.cancel(true);
					    System.out.println("...Timed out.");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				else {
					System.out.println("There is no stored value on that key");
				}
			}
			
			else if (input.substring(0, 3).equals("GET")){
				String firstSplit = input.split("\\(")[1];
				String secondSplit = firstSplit.split("\\)")[0];
				final int key = Integer.parseInt(secondSplit);
				
				if (map.containsKey(key)){
					value = map.get(key);
					System.out.println("You got value '" +value+ "' from key "+key);
				}
				else {
					
					System.out.println("Looking for key...");
					ExecutorService executorService = Executors.newSingleThreadExecutor();
					String message = "GET("+key+","+N.myPort+")";
					GetClient.SendMessageAndListen s = new GetClient.SendMessageAndListen(N.friends.get(0), message);
					Future<String> future = executorService.submit(s);
					try {
					    String result = future.get(5, TimeUnit.SECONDS);
					    return result;
					} catch (TimeoutException e) {
					    future.cancel(true);
					    System.out.println("...Timed out.");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
			}
			
			else{
				System.out.println("Don't understand input: "+input);
				
			}
			
			//System.out.println(map.toString());
			
			return value;
			
		}
	}
	
	static class RequireNeighbours implements Callable<String> {
		int port;
		
		public RequireNeighbours (int neighbour) {
			this.port = neighbour;
		}
		
		@Override
		public String call() throws Exception {
			
			Socket s = null;
			
			try{
				s = new Socket("localhost", port);
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out = new DataOutputStream( s.getOutputStream());
				String message = "GETNEIGHBOURS";
				out.writeUTF(message); // UTF is a string encoding; see Sec 4.3
				String value = in.readUTF();
				return value;
			}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
			} catch (EOFException e){System.out.println("Found no value");
			} catch (IOException e){System.out.println("IO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
			
			return null;
		}
		
	}
	
	static class RequireDublicates implements Callable<String> {
		int port;
		
		public RequireDublicates (int neighbour) {
			this.port = neighbour;
		}
		
		@Override
		public String call() throws Exception {
			
			Socket s = null;
			
			try{
				s = new Socket("localhost", port);
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out = new DataOutputStream( s.getOutputStream());
				String message = "GETDUBLICATES";
				out.writeUTF(message); // UTF is a string encoding; see Sec 4.3
				String value = in.readUTF();
				return value;
			}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
			} catch (EOFException e){System.out.println("Found no value");
			} catch (IOException e){System.out.println("IO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
			
			return null;
		}
		
	}
	
	static class InformNeighbours implements Callable<String> {
		int port;
		String message;
		
		public InformNeighbours (int neighbour, String str) {
			this.port = neighbour;
			this.message = str;
		}
		
		@Override
		public String call() throws Exception {
			
			Socket s = null;
			
			try{
				s = new Socket("localhost", port);
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out = new DataOutputStream( s.getOutputStream());
				String m = "UPDATE:"+message;
				out.writeUTF(m); // UTF is a string encoding; see Sec 4.3
				String value = in.readUTF();
				return value;
			}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
			} catch (EOFException e){System.out.println("Found no value");
			} catch (IOException e){System.out.println("IO:"+e.getMessage());
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
			
			return null;
		}
		
	}
	
	static class HeartBeatThread extends Thread {
		Node N;
		TimerTask t;
		Timer timer;
		
		public HeartBeatThread (Node n) {
			this.N = n;
			this.timer = new Timer();
			this.t=new TimerTask(){
				public void run(){
					
					int port = N.friends.get(0);
					if (port!=0)
						new SendHeartBeat(N);
				}
			};
			this.start();
		}

		public void run(){
			timer.scheduleAtFixedRate(t, 0, 2000);	
		}
		
		public void update(Node n2){
			//Otherwise it is not able to see the updated left neighbour.
			//Could have made N volatile, but this is more thread-safe
			this.N = n2;
		}

		public void cancel() {
			timer.cancel();
			//System.out.println("Timer was cancelled");
		}
		
	}
	
	static class SendHeartBeat extends Thread{
		Node N;
		
		public SendHeartBeat(Node n) {
			this.N = n;
			this.start();
		}

		@Override
		public void run() {
			Socket s = null;
			
			try{
				int serverPort = N.friends.get(0);
				s = new Socket("localhost", serverPort);
				DataInputStream in = new DataInputStream( s.getInputStream());
				DataOutputStream out = new DataOutputStream( s.getOutputStream());
				String message = "HEARTBEAT";
				out.writeUTF(message); 
				String value = in.readUTF();
				//System.out.println(value);
			}catch (UnknownHostException e){
			System.out.println("Sock:"+e.getMessage());
			} catch (EOFException e){System.out.println("EOF:"+e.getMessage());
			} catch (IOException e){
				System.out.println("No heartbeat!");
				System.out.println("Re-routing...");
				N.heartBeat.cancel();
				N.reRoute();
			} finally {if(s!=null) try {s.close();}catch (IOException e){/*close failed*/}}
			
			
		}
		
	}

	
		
}

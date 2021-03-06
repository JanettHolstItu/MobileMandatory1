//Dataloss Server

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
public class DL_UDPServer{
	public static ConcurrentHashMap<Integer, Boolean> messages;
	
	public static int numOfDatagramsSent;
	public static int transmissionInterval;
	public static int timeToReceive;
	
	public static int dublication;
	
	volatile static boolean waitingStill = true;
	static Thread t;
	
	public static void main(String args[]){
		
		timeToReceive = 17;				//args[0];
		numOfDatagramsSent = 1000;		//args[1];
		transmissionInterval = 0;		//args[2];
		
		dublication = 0;
		
		
		messages = new ConcurrentHashMap<Integer, Boolean>(1000);
		for (int i=0; i<numOfDatagramsSent; i++){
			messages.put(i, false);
		}
		
		
		DatagramSocket aSocket = null;
		try{
			aSocket = new DatagramSocket(6789);
			byte[] buffer = new byte[1000];
			t = new Thread() {
			    public void run() {
			        for (int i=0; i<timeToReceive;i++){
			        	try {
							sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        	System.out.println((i+1) + " seconds");
			        }
			        
			        System.out.println("Interval between transmissions: "+ transmissionInterval + " ms");
			        System.out.println(percentageLoss());
			        System.out.println(percentageDublication());
			        
			        
			    }
			};
			t.start();
			while(true){
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String message = new String(request.getData());
				Integer int_m = Integer.parseInt(message.split("#")[0]);
				//System.out.println("Message: "+message);
				if (messages.contains(int_m)) dublication +=1;
				messages.put(int_m, true);
				
			}
			
			
			
			
		} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null) aSocket.close();
			
		}
	}

	//absolute number and percentage of duplicated datagrams
	protected static String percentageDublication() {
		String s = "Dublicated messages: "+dublication + ", Percentage: "+((100./numOfDatagramsSent)*dublication)+"%";
		return s;
	}

	//absolute number and percentage of lost datagrams
	protected static String percentageLoss() {
		//System.out.println("We are missing the following messages:");
        int count = 0;
        for (Integer key : messages.keySet()) {
			if (messages.get(key)==false){
				count += 1;
			}
			
		}
        String s = "Lost messages: "+count + ", Percentage: " +((100./numOfDatagramsSent)*count) + "%";
        return s;
	}
}
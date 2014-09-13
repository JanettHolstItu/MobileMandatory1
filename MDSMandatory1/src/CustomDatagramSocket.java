import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class CustomDatagramSocket extends DatagramSocket {

	public CustomDatagramSocket() throws SocketException {
		super();
	}

	
	public void send(DatagramPacket arg0) throws IOException {
		byte[] data = arg0.getData();
		
		
		System.out.println("String: "+new String(data));
		
		byte temp = data[2];
		data[2] = data[5];
		data[5] = temp;
		
		System.out.println("String: "+new String(data));
		
		
		
		super.send(arg0);
	}
	

}

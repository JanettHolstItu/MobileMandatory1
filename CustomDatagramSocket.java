import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;


public class CustomDatagramSocket extends DatagramSocket {

	public CustomDatagramSocket() throws SocketException {
		super();
	}

	public void send(DatagramPacket arg0) throws IOException {
		// Fuck up the DatagramPacket here
		super.send(arg0);
	}
	
	

}

##################################################################################################
HOW TO USE THE SYSTEM:
##################################################################################################

(The system is initially set up to be run locally on one computer)

1. Run TCPServer.java

2. Run any of the following:

	- TCPSink: press 'Enter' to register, and 'Exit' to unregister.
	
	- TCPSource: press 'Enter' to register, and 'Exit' to unregister.
		While registered: Type anything else than other than 'Exit', and the message
		will be delivered to all registered Sinks.


##################################################################################################
HOW THE SYSTEM WORKS:
##################################################################################################

1. When TCPServer is started, it starts a thread that will always listen for new connections.
Any new connection will be treated in a new thread. 
The thread will react on the following message contents:
	- "EnterSink": It registers a port number for the sink in a thread-safe ArrayList (SyncedArray.java), sends back the stored 
		port number to the sink, and closes the connection.
	- "ExitSink": It removes the stored port number from the thread-safe ArrayList (SyncedArray.java), and closes the connection.
	- "EnterSrc:" It opens a connection to the client, and keeps it open. For all the following messages
		received through this open connection, TCPServer creates a new thread and a new connection to all Sinks identified through 
		the port numbers registered in the SyncedArray ArrayList. After each message is sent, each associated connection and thread is 
		closed again.
		If the incoming source message says 'exit' or 'Exit', the Source connection and thread is closed.
		
2. TCPSink: When registering, TCPSink sends a message containing the key 'EnterSink'. It then receives the port number which was used, 
	before that connection is closed. The port number, which it is registered with, is stored in a variable.
	After registration, a TCP server connection is opened on the memorized port in a new thread to listen for messages.
	On the same time, TCPSink listens on System.in. If System.in returns 'exit', a message is sent to TCPServer to unregister, and the connection
	is closed. The connection can be reopened, if System.in returns 'enter' again.
	
3. TCPSource registers and unregisters the same way as TCPSink. The difference is that after registering, the connection is not closed.
	The connection is kept open for the source to publish messages to TCPServer which will forward all messages to all registered Sinks.
	TCPSource also keeps listening on System.in to know when to enter and exit.
	
	
	
	
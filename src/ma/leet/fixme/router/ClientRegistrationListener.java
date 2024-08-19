import java.nio.channels.SocketChannel;

/**
 * This interface represents a listener for client registration events.
 */
public interface ClientRegistrationListener {
	/**
	 * Called when a client is connected to the server.
	 *
	 * @param client the connected client's SocketChannel
	 * @param port   the port number on which the client is connected
	 */
	void onClientConnected(SocketChannel client, int port);
}

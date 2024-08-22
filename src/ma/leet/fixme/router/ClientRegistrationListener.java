import java.nio.channels.SocketChannel;

/**
 * This interface represents a listener for client registration events.
 */
public interface ClientRegistrationListener {
	String onClientConnected(SocketChannel client, int port);
	void onClientDisconnected(SocketChannel client);
}

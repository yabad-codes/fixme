import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The Router class represents a router that handles client connections and disconnections.
 * It implements the ClientRegistrationListener interface.
 */
public class Router implements ClientRegistrationListener {
	private final RoutingTable routingTable = new RoutingTable();
	private final RouterServer routerServer;

	/**
	 * The Router class constructor.
	 */
	public Router() throws IOException {
		routerServer = new RouterServer(this);
		routerServer.start();
	}

	/**
	 * Handles the event when a client is connected to the router.
	 * Adds the client to the routing table and prints the client type and ID.
	 *
	 * @param client The SocketChannel representing the connected client.
	 * @param port The port number on which the client is connected.
	 */
	public String onClientConnected(SocketChannel client, int port) {
		String id = routingTable.addClient(client);
		String clientType = (port == RouterServer.BROKER_PORT) ? "Broker" : "Market";
		System.out.println(clientType + " client connected with ID: " + id);
		return id;
	}

	/**
	 * Handles the event when a client is disconnected.
	 * Removes the client from the routing table and prints a message indicating the disconnection.
	 *
	 * @param client the SocketChannel representing the disconnected client
	 */
	public void onClientDisconnected(SocketChannel client) {
		String clientId = routingTable.getClientId(client);
		RoutingTable.ClientType clientType = routingTable.getClientType(clientId);
		routingTable.removeClient(clientId);
		System.out.println(clientType.toString() + " client with ID: " + clientId + " disconnected.");
	}

	public static void main(String[] args) {
		try {
			new Router();
		} catch (IOException e) {
			System.out.println("Error starting router: " + e.getMessage());
		}
	}
}

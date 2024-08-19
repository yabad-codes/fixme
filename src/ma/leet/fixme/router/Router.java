import java.nio.channels.SocketChannel;

public class Router implements ClientRegistrationListener {
	private final RoutingTable routingTable = new RoutingTable();
	private final RouterServer routerServer;

	public Router() {
		this.routerServer = new RouterServer(this);
	}

	public void onClientConnected(SocketChannel client, int port) {
		String id = routingTable.addClient(client);
		String clientType = (port == RouterServer.BROKER_PORT) ? "Broker" : "Market";
		System.out.println(clientType + " client connected with ID: " + id);
	}

	public static void main(String[] args) {
		new Router();
	}
}

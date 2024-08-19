import java.util.Map;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RoutingTable {
	private final Map<String, SocketChannel> routingTable;
	private final Random random;

	public enum ClientType {
		BROKER, MARKET
	}

	public RoutingTable() {
		routingTable = new ConcurrentHashMap<>();
		random = new Random();
	}

	public String addClient(SocketChannel client) {
		String id = generateUniqueId();
		routingTable.put(id, client);
		return id;
	}

	public void removeClient(String id) {
		routingTable.remove(id);
	}

	public SocketChannel getClient(String id) {
		return routingTable.get(id);
	}

	public ClientType getClientType(String id) {
		int clientPort = routingTable.get(id).socket().getLocalPort();
		return (clientPort == RouterServer.BROKER_PORT) ? ClientType.BROKER : ClientType.MARKET;
	}

	public boolean containsClient(String id) {
		return routingTable.containsKey(id);
	}

	public int size() {
		return routingTable.size();
	}

	public void clear() {
		routingTable.clear();
	}

	public void print() {
		System.out.println("Routing Table : ");
		routingTable.forEach((key, value) -> System.out.println(key + " -> " + value));
	}

	private String generateUniqueId() {
		String id;

		do {
			id = String.format("%06d", random.nextInt(1000000));
		} while (routingTable.containsKey(id));
		return id;
	}
}

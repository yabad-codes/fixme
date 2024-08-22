import java.util.Map;
import java.util.Random;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The RoutingTable class represents a routing table that maps unique client IDs to SocketChannels.
 * It provides methods to add and remove clients, retrieve client information, and perform other operations on the routing table.
 */
public class RoutingTable {
	private final Map<String, SocketChannel> routingTable;
	private final Random random;

	/**
	 * Represents the type of a client in the routing table.
	 */
	public enum ClientType {
		BROKER, MARKET
	}

	/**
	 * This class represents a routing table that stores information about the available routes in a network.
	 */
	public RoutingTable() {
		routingTable = new ConcurrentHashMap<>();
		random = new Random();
	}

	/**
	 * Adds a client to the routing table.
	 * 
	 * @param client the SocketChannel representing the client
	 * @return the unique identifier assigned to the client
	 */
	public String addClient(SocketChannel client) {
		String id = generateUniqueId();
		routingTable.put(id, client);
		return id;
	}

	/**
	 * Removes a client from the routing table.
	 *
	 * @param id the ID of the client to be removed
	 */
	public void removeClient(String id) {
		routingTable.remove(id);
	}

	/**
	 * Retrieves the SocketChannel associated with the given ID.
	 *
	 * @param id the ID of the client
	 * @return the SocketChannel associated with the ID, or null if not found
	 */
	public SocketChannel getClient(String id) {
		return routingTable.get(id);
	}

	/**
	 * Retrieves the client ID associated with the given SocketChannel.
	 *
	 * @param client the SocketChannel for which to retrieve the client ID
	 * @return the client ID associated with the given SocketChannel, or null if not found
	 */
	public String getClientId(SocketChannel client) {
		for (Map.Entry<String, SocketChannel> entry : routingTable.entrySet()) {
			if (entry.getValue().equals(client)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Retrieves the client type based on the provided ID.
	 *
	 * @param id The ID of the client.
	 * @return The client type (BROKER or MARKET).
	 */
	public ClientType getClientType(String id) {
		int clientPort = routingTable.get(id).socket().getLocalPort();
		return (clientPort == RouterServer.BROKER_PORT) ? ClientType.BROKER : ClientType.MARKET;
	}

	/**
	 * Checks if the routing table contains a client with the specified ID.
	 *
	 * @param id the ID of the client to check
	 * @return true if the routing table contains the client, false otherwise
	 */
	public boolean containsClient(String id) {
		return routingTable.containsKey(id);
	}

	/**
	 * Returns the number of entries in the routing table.
	 *
	 * @return the size of the routing table
	 */
	public int size() {
		return routingTable.size();
	}

	/**
	 * Clears the routing table.
	 */
	public void clear() {
		routingTable.clear();
	}

	/**
	 * Prints the routing table.
	 */
	public void print() {
		System.out.println("Routing Table : ");
		routingTable.forEach((key, value) -> System.out.println(key + " -> " + value));
	}

	/**
	 * Generates a unique identifier as a String.
	 *
	 * @return The generated unique identifier.
	 */
	private String generateUniqueId() {
		String id;

		do {
			id = String.format("%06d", random.nextInt(1000000));
		} while (routingTable.containsKey(id));
		return id;
	}
}

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The RouterServer class represents a server that routes messages between clients.
 * It listens on two ports, one for brokers and one for markets, and uses a thread pool
 * to handle incoming client connections and messages.
 */
public class RouterServer {
	public static final int BROKER_PORT = 5000;
	public static final int MARKET_PORT = 5001;
	private static final String HOST = "localhost";
	private static final int BUFFER_SIZE = 1024;
	private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

	private Selector selector;
	private ServerSocketChannel brokerSocket;
	private ServerSocketChannel marketSocket;
	private ExecutorService threadPool;
	private ClientRegistrationListener listener;

	/**
	 * The RouterServer class constructor initializes the server by creating server socket channels 
	 * @param listener The client registration listener to be notified of new client registrations.
	 * @throws IOException If an I/O error occurs while creating the server socket channels.
	 */
	public RouterServer(ClientRegistrationListener listener) throws IOException {
		this.listener = listener;
		this.selector = Selector.open();
		this.brokerSocket = createServerSocketChannel(BROKER_PORT);
		this.marketSocket = createServerSocketChannel(MARKET_PORT);
		this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	}

	/**
	 * Starts the router server and enters the main server loop.
	 * This method continuously selects and processes the selected keys
	 * until an IOException occurs or the server is shutdown.
	 */
	public void start() {
		try {
			while (true) {
				selector.select();
				processSelectedKeys();
			}
		} catch (IOException e) {
			System.out.println("Error in main server loop: " + e.getMessage());
		} finally {
			shutdown();
		}
	}

	/**
	 * Creates a ServerSocketChannel and binds it to the specified port.
	 *
	 * @param port the port number to bind the ServerSocketChannel to
	 * @return the created ServerSocketChannel
	 * @throws IOException if an I/O error occurs while opening the ServerSocketChannel
	 */
	private ServerSocketChannel createServerSocketChannel(int port) throws IOException {
		ServerSocketChannel serverSocket = ServerSocketChannel.open();
		serverSocket.bind(new InetSocketAddress(HOST, port))
				.configureBlocking(false)
				.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Listening on port " + port);
		return serverSocket;
	}

	/**
	 * Processes the selected keys from the selector.
	 * This method iterates through the selected keys and performs the appropriate actions based on the key's operations.
	 * If a key is acceptable, it accepts the connection.
	 * If a key is readable, it sets the interestOps to 0 and submits a task to the thread pool to read the message.
	 *
	 * @throws IOException if an I/O error occurs
	 */
	private void processSelectedKeys() throws IOException {
		Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
		while (keyIterator.hasNext()) {
			SelectionKey key = keyIterator.next();
			keyIterator.remove();

			if (!key.isValid())
				continue;

			if (key.isAcceptable()) {
				String id = acceptConnection(key);
				// send id to client
			} else if (key.isReadable()) {
				key.interestOps(0);
				threadPool.submit(() -> readMessage(key));
			}
		}
	}

	/**
	 * Accepts a connection from a client and returns a String representing the result.
	 *
	 * @param key the SelectionKey associated with the server socket
	 * @return a String representing the result of accepting the connection
	 * @throws IOException if an I/O error occurs while accepting the connection
	 */
	private String acceptConnection(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
		SocketChannel client = serverSocket.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);

		int port = (serverSocket == brokerSocket) ? BROKER_PORT : MARKET_PORT;
		return listener.onClientConnected(client, port);
	}

	/**
	 * Reads a message from the client associated with the given SelectionKey.
	 * 
	 * @param key the SelectionKey associated with the client
	 */
	private void readMessage(SelectionKey key) {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

		try {
			int bytesRead = client.read(buffer);
			if (bytesRead > 0) {
				processMessage(buffer, bytesRead);
				key.interestOps(SelectionKey.OP_READ);
				key.selector().wakeup();
			} else if (bytesRead == -1) {
				listener.onClientDisconnected(client);
				cancelKey(key);
			}
		} catch (IOException e) {
			System.out.println("Error reading from client: " + e.getMessage());
			cancelKey(key);
		}
	}

	/**
	 * Processes the received message from the client.
	 * 
	 * @param buffer The ByteBuffer containing the received message.
	 * @param bytesRead The number of bytes read from the ByteBuffer.
	 */
	private void processMessage(ByteBuffer buffer, int bytesRead) {
		buffer.flip();
		String message = new String(buffer.array(), 0, bytesRead).trim();
		System.out.println("Received message: " + message);
		// FIX Messages Parsing and Processing
	}

	/**
	 * Cancels the given SelectionKey and closes its associated channel.
	 *
	 * @param key the SelectionKey to cancel and close
	 */
	private void cancelKey(SelectionKey key) {
		try {
			key.cancel();
			key.channel().close();
		} catch (IOException e) {
			System.err.println("Error closing client connection: " + e.getMessage());
		}
	}

	/**
	 * Shuts down the router server.
	 * This method stops the thread pool, closes the selector, broker socket, and market socket.
	 * Any IOException that occurs during the shutdown process will be printed to the standard error stream.
	 */
	private void shutdown() {
		threadPool.shutdownNow();
		try {
			selector.close();
			brokerSocket.close();
			marketSocket.close();
		} catch (IOException e) {
			System.err.println("Error during server shutdown: " + e.getMessage());
		}
	}
}

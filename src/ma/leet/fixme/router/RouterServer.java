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

	public RouterServer(ClientRegistrationListener listener) {
		this.listener = listener;
		init();
		start();
	}

	private void init() {
		try {
			// Open a selector, broker socket, and market socket
			selector = Selector.open();
			brokerSocket = ServerSocketChannel.open();
			marketSocket = ServerSocketChannel.open();

			// Bind the broker and market sockets to the host and port
			brokerSocket.bind(new InetSocketAddress(HOST, BROKER_PORT)).configureBlocking(false).register(selector,
					SelectionKey.OP_ACCEPT);
			marketSocket.bind(new InetSocketAddress(HOST, MARKET_PORT)).configureBlocking(false).register(selector,
					SelectionKey.OP_ACCEPT);

			// Initialize the thread pool
			threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

			// Log the server initialization
			System.out.println(
					"Router Server is listening for Markets and Brokers on " + MARKET_PORT + " and " + BROKER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void start() {
		try {
			while (true) {
				// select the keys
				selector.select();

				// get the selected keys
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();

					// check for key validity
					if (!key.isValid())
						continue;

					// check if the key is acceptable or readable
					if (key.isAcceptable())
						accept(key);
					else if (key.isReadable()) {
						key.interestOps(0);
						threadPool.submit(() -> read(key));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
			int port = (serverSocket == brokerSocket) ? BROKER_PORT : MARKET_PORT;
			SocketChannel client = serverSocket.accept();
			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
			listener.onClientConnected(client, port);

			// Send the id to the client and receive acknowledgment
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void read(SelectionKey key) {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

		try {
			int bytesRead = client.read(buffer);

			if (bytesRead > 0) {
				buffer.flip();
				String message = new String(buffer.array(), 0, bytesRead).trim();
				System.out.println("Received message: " + message);
				key.interestOps(SelectionKey.OP_READ);
				key.selector().wakeup();
			} else if (bytesRead == -1) {
				System.out.println("Client disconnected");
				cancelKey(key);
			}
		} catch (IOException e) {
			System.out.println("Error reading from client: " + e.getMessage());
			cancelKey(key);
		}
	}

	private void cancelKey(SelectionKey key) {
		try {
			key.cancel();
			key.channel().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package ma.leet.fixme.market;

public interface Market {
	public enum MarketType {
		STOCK, EXCHANGE, CRYPTO, FUTURES
	}

	void connect();

	void disconnect();

	void buy();

	void sell();
}

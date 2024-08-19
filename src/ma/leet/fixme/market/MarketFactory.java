package ma.leet.fixme.market;

public class MarketFactory {
	public static Market createMarket(Market.MarketType type) {
		switch (type) {
			case STOCK:
				return new StockMarket();
			case EXCHANGE:
				return new ExchangeMarket();
			case CRYPTO:
				return new CryptoMarket();
			case FUTURES:
				return new FuturesMarket();
			default:
				throw new IllegalArgumentException("Unsupported market type: " + type);
		}
	}
}

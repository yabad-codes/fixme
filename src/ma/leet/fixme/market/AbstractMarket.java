package ma.leet.fixme.market;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import ma.leet.fixme.market.instrument.Instrument;
import ma.leet.fixme.market.instrument.InstrumentFactory;

public abstract class AbstractMarket implements Market {
	protected SocketChannel socketChannel;
	protected String id;
	protected MarketType type;
	protected String filePath;
	protected List<Instrument> instruments;

	public AbstractMarket(MarketType type) {
		String path = System.getProperty("user.dir") + "/src/ma/leet/fixme/test/instruments/";
		switch (type) {
			case STOCK:
				this.filePath = path + "stock.txt";
				break;
			case EXCHANGE:
				this.filePath = path + "exchange.txt";
				break;
			case CRYPTO:
				this.filePath = path + "crypto.txt";
				break;
			default:
				this.filePath = "";
				break;
		}
		loadInstruments();
	}

	public void loadInstruments() {
		try {
			this.instruments = InstrumentFactory.createInstrumentsFromFile(filePath, MarketType.STOCK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Instrument> getInstruments() {
		return instruments;
	}

	public void printInstruments() {
		System.out.println("Instruments:");
		for (Instrument instrument : instruments) {
			System.out.println(instrument.getSymbol() + " " + instrument.getName() + " " + instrument.getPrice() + " "
					+ instrument.getQuantity());
		}
	}
}

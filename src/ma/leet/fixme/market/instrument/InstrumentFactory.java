package ma.leet.fixme.market.instrument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ma.leet.fixme.market.Market;

public class InstrumentFactory {
	public static List<Instrument> createInstrumentsFromFile(String filepath, Market.MarketType type) throws IOException {
		List<Instrument> instruments = new ArrayList<>();

		try (
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
		) {
			String line;

			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\t");
				if (parts.length != 4)
					continue;
				
				String symbol = parts[0];
				String name = parts[1];
				double price = Double.parseDouble(parts[2]);
				int quantity = Integer.parseInt(parts[3]);

				Instrument instrument = createInstrument(type, symbol, name, price, quantity);
				if (instrument != null)
					instruments.add(instrument);
			}

			return instruments;
		}
	}

	private static Instrument createInstrument(Market.MarketType type, String symbol, String name, double price, int quantity) {
		switch (type) {
			case STOCK:
				return new Stock(symbol, name, price, quantity);
			case EXCHANGE:
				return new Exchange(symbol, name, price, quantity);
			case CRYPTO:
				return new Crypto(symbol, name, price, quantity);
			default:
				return null;
		}
	}
}

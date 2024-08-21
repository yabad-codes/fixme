package ma.leet.fixme.market.instrument;

public abstract class AbstractInstrument implements Instrument {
	protected String symbol;
	protected String name;
	protected double price;
	protected int quantity;

	public AbstractInstrument(String symbol, String name, double price, int quantity) {
		this.symbol = symbol;
		this.name = name;
		this.price = price;
		this.quantity = quantity;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public int getQuantity() {
		return quantity;
	}

	@Override
	public void setPrice(double price) {
		if (price < 0) {
			this.price = 0;
		}
		this.price = price;
	}

	@Override
	public void setQuantity(int quantity) {
		if (quantity < 0) {
			this.quantity = 0;
		}
		this.quantity = quantity;
	}
}

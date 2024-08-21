package ma.leet.fixme.market.instrument;

public interface Instrument {
	String getSymbol();
	String getName();
	double getPrice();
	int getQuantity();

	void setQuantity(int quantity);
	void setPrice(double price);
}

package io.icw.transaction.model.bo;

import java.util.ArrayList;
import java.util.List;

public class Match {
	private String hash = "123";
	private Order order;
	private List<Trade> trades = new ArrayList<Trade>();

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public List<Trade> getTrades() {
		return trades;
	}

	public void setTrades(List<Trade> trades) {
		this.trades = trades;
	}
	
	@Override
	public String toString() {
		return "Match [order=" + order + ", trades=" + trades + "]";
	}
}

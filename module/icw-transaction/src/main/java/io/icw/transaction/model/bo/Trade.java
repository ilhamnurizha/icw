package io.icw.transaction.model.bo;

import java.math.BigInteger;

public class Trade {
	private String hash;
	private Order buy;
	private Order sell;
	private BigInteger otherAmount;
	private BigInteger thisAmount;


	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public BigInteger getOtherAmount() {
		return otherAmount;
	}

	public void setOtherAmount(BigInteger otherAmount) {
		this.otherAmount = otherAmount;
	}

	public BigInteger getThisAmount() {
		return thisAmount;
	}

	public void setThisAmount(BigInteger thisAmount) {
		this.thisAmount = thisAmount;
	}

	public Order getBuy() {
		return buy;
	}

	public void setBuy(Order buy) {
		this.buy = buy;
	}

	public Order getSell() {
		return sell;
	}

	public void setSell(Order sell) {
		this.sell = sell;
	}

	@Override
	public String toString() {
		return "Trade [buy=" + buy + ", sell=" + sell + ", otherAmount=" + otherAmount + ", thisAmount=" + thisAmount
				+ "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buy == null) ? 0 : buy.hashCode());
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((otherAmount == null) ? 0 : otherAmount.hashCode());
		result = prime * result + ((sell == null) ? 0 : sell.hashCode());
		result = prime * result + ((thisAmount == null) ? 0 : thisAmount.hashCode());
		return result;
	}
}

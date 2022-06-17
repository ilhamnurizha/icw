package io.icw.transaction.model.bo;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Order {
	private int type = 1; // 1买入 2卖出
	private BigInteger otherAmount = BigInteger.ZERO;
	private BigInteger thisAmount = BigInteger.ZERO;
	private BigInteger origOtherAmount = BigInteger.ZERO;
	private BigInteger origThisAmount = BigInteger.ZERO;
	private String lockhash = null;
	private String address = null;
	private double price;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	public String getLockhash() {
		return lockhash;
	}

	public void setLockhash(String lockhash) {
		this.lockhash = lockhash;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getPrice() {
		return price;
	}
	
	public void caclPrice() {
		this.price = new BigDecimal(otherAmount.toString()).setScale(10).divide(new BigDecimal(thisAmount.toString()).setScale(10), 0).doubleValue();
	}
	
	public BigInteger getOrigOtherAmount() {
		return origOtherAmount;
	}

	public void setOrigOtherAmount(BigInteger origOtherAmount) {
		this.origOtherAmount = origOtherAmount;
	}

	public BigInteger getOrigThisAmount() {
		return origThisAmount;
	}

	public void setOrigThisAmount(BigInteger origThisAmount) {
		this.origThisAmount = origThisAmount;
	}

	@Override
	public String toString() {
		return "Order [type=" + type + ", otherAmount=" + otherAmount + ", thisAmount=" + thisAmount
				+ ", origOtherAmount=" + origOtherAmount + ", origThisAmount=" + origThisAmount + ", lockhash="
				+ lockhash + ", address=" + address + ", price=" + price + "]";
	}
}

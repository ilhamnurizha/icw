package io.icw.api.model.po;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.bson.Document;

import io.icw.api.utils.DocumentTransferTool;

public class Order {
	private String orderNo;
	private int type = 1; // 1买入 2卖出
	private BigInteger otherAmount = BigInteger.ZERO;
	private BigInteger thisAmount = BigInteger.ZERO;
	private BigInteger origOtherAmount = BigInteger.ZERO;
	private BigInteger origThisAmount = BigInteger.ZERO;
	private String address = null;
	private double price;
	private long time;
	private String txhash;
	private String pairAddress = null;
	private String contractAddress = null;

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public String getPairAddress() {
		return pairAddress;
	}

	public void setPairAddress(String pairAddress) {
		this.pairAddress = pairAddress;
	}
	
	public Document toDocument() {
        Document document = DocumentTransferTool.toDocument(this, "orderNo");
        return document;
	}
	
	public static Order toInfo(Document document) {
		Order resultInfo = DocumentTransferTool.toInfo(document, "orderNo", Order.class);
        return resultInfo;
    }
	
	public String getTxhash() {
		return txhash;
	}

	public void setTxhash(String txhash) {
		this.txhash = txhash;
	}
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
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
		return "Order [orderNo=" + orderNo + ", type=" + type + ", otherAmount=" + otherAmount + ", thisAmount="
				+ thisAmount + ", origOtherAmount=" + origOtherAmount + ", origThisAmount=" + origThisAmount
				+ ", address=" + address + ", price=" + price + "]";
	}
}

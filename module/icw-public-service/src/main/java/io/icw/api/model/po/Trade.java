package io.icw.api.model.po;

import java.math.BigInteger;

import org.bson.Document;

import io.icw.api.utils.DocumentTransferTool;

public class Trade {
	private String hash;
	private int type = 1; // 1买入 2卖出
	private Order buy;
	private Order sell;
	private BigInteger otherAmount;
	private BigInteger thisAmount;
	private long time;
	private String txhash;
	private String pairAddress = null;
	private String contractAddress = null;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
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
        Document document = new Document();
        document.put("_id", hash);
        document.put("otherAmount", otherAmount.toString());
        document.put("thisAmount", thisAmount.toString());
        document.put("time", time);
        document.put("txhash", txhash);
        document.put("pairAddress", pairAddress);
        document.put("contractAddress", contractAddress);

        Document buyDoc = buy.toDocument();
        document.put("buy", buyDoc);

        Document sellDoc = sell.toDocument();
        document.put("sell", sellDoc);
        return document;
	}
	
	public static Trade toInfo(Document document) {
		Trade resultInfo = DocumentTransferTool.toInfo(document, "hash", Trade.class);
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

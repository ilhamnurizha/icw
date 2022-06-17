package io.icw.api.rpc.controller;

public class FeeInfoStr {
    private int chainId;

    private int assetId;

    private String symbol;

    private String value;

    public FeeInfoStr() {

    }

    public FeeInfoStr(int chainId, int assetId, String symbol) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
        this.value = "0";
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

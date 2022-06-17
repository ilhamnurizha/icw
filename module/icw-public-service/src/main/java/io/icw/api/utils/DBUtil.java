package io.icw.api.utils;

import io.icw.api.constant.DBTableConstant;

public class DBUtil {

    public static int getShardNumber(String value) {
        return Math.abs(value.hashCode()) % DBTableConstant.TX_RELATION_SHARDING_COUNT;
    }

    public static String getAssetKey(int chainId, int assetId) {
        return chainId + "-" + assetId;
    }

    public static String getAccountAssetKey(String address, int chainId, int assetId) {
        return address + "-" + chainId + "-" + assetId;
    }

    public static String getDepositKey(String hash, String key) {
        return hash + "-" + key;
    }
    
    public static void main(String [] args) {
    	System.out.println(getShardNumber("EDAOd6HghAd4Jzrq6V3L8UaLnkt5kMebzHBhM"));
    }
}

package io.icw;

import java.io.File;
import java.math.BigInteger;

import io.icw.core.core.annotation.Configuration;

@Configuration(domain = "pow")
public class Config {
    private int chainId;

    private int assetId;

    private BigInteger powFee;

    private String dataPath;

    private long round;
    
    private int packNumber;
    
    private int diff;
    
    private int stop;
    
    public int getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public int getDiff() {
		return diff;
	}

	public void setDiff(int diff) {
		this.diff = diff;
	}

	public int getPackNumber() {
		return packNumber;
	}

	public void setPackNumber(int packNumber) {
		this.packNumber = packNumber;
	}

	public long getRound() {
		return round;
	}

	public void setRound(long round) {
		this.round = round;
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

    public String getDataPath() {
        return dataPath + File.separator + "pow";
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public BigInteger getPowFee() {
        return powFee;
    }

    public void setPowFee(BigInteger powFee) {
        this.powFee = powFee;
    }
}

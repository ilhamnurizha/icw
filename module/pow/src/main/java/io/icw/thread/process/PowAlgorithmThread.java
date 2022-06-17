package io.icw.thread.process;

import java.util.HashMap;
import java.util.Map;

import io.icw.base.data.BlockHeader;
import io.icw.core.log.Log;
import io.icw.pow.BlockPow;
import io.icw.pow.PowAlgorithm;

public class PowAlgorithmThread extends Thread {
	private Map<Long, BlockPow> roundMap = new HashMap<Long, BlockPow>();
	private String packingAddress;
	private long round = 0;
	private BlockHeader blockHeader = null;
	private boolean start = false;
	
	public BlockHeader getBlockHeader() {
		return blockHeader;
	}

	public void setBlockHeader(long round, BlockHeader blockHeader, String packingAddress) {
		this.blockHeader = blockHeader;
//		Log.info("round: " + round + " this.round: " + this.round);
		if (round != this.round) {
			if (this.round != 0)  {
				start = true;
			}

			roundMap.remove(this.round);
			
			this.round = round;
			this.packingAddress = packingAddress;
			
			PowAlgorithm.stop = true;
		} else {
			this.round = round;
			this.packingAddress = packingAddress;
			
			PowAlgorithm.stop = false;
		}
	}
	
	public BlockPow getBlockPow(long round) {
		return roundMap.get(round);
	}
	
	public void run() {
		while (true) {
			try {
//				Log.info("round: " + round);
				if (start && roundMap.get(round) == null) {
					long timeMillis = System.currentTimeMillis();
					BlockPow blockPow = new BlockPow();
					blockPow.setTimestamp(timeMillis);
					blockPow.setHeight(blockHeader.getHeight());
					blockPow.setIndex(round);
					blockPow.setPreHash(blockHeader.getHash().toString());
					blockPow.setNonce(0);
					blockPow.setAddress(packingAddress);
					blockPow.setPowTimestamp(timeMillis);
					
					blockPow.setDiff(PowProcess.getCalculateDiff(round, blockPow.getHeight()));
//					Log.info("hash: " + PowAlgorithm.hash + ":"  + Thread.currentThread().getId() + ":" + Thread.currentThread().getName());
					PowAlgorithm.pow(blockPow.getDiff(), blockPow);
//					Log.info("hash: " + PowAlgorithm.hash + ":"  + Thread.currentThread().getId() + ":" + Thread.currentThread().getName());
//					Log.info("blockPow: " + blockPow);
					
//					Log.info("getRoundMembersByHeight: " 
//							+ PowProcess.getRoundMembersByHeight(round, blockHeader.getHeight()).size() 
//							+ " getPackNumber: " + PowProcess.config.getPackNumber());
					
					if (PowAlgorithm.hash != null
							&& PowProcess.getRoundMembersByHeight(round, blockHeader.getHeight()).size() < PowProcess.config.getPackNumber()) {
						blockPow.setHashCode(PowAlgorithm.hash);
						
						Log.info("blockPow: " + blockPow);
						
						roundMap.clear();
						roundMap.put(round, blockPow);
					}
				} else {
					Thread.sleep(100);
				}
			} catch (Exception e) {
//				e.printStackTrace();
				Log.error(e.getMessage(), e);
			}
		}
	}
}

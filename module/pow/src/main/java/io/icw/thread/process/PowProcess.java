package io.icw.thread.process;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import io.icw.Config;
import io.icw.Constant;
import io.icw.Utils;
import io.icw.base.RPCUtil;
import io.icw.base.basic.AddressTool;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.Block;
import io.icw.base.data.BlockExtendsData;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.CoinData;
import io.icw.base.data.CoinFrom;
import io.icw.base.data.CoinTo;
import io.icw.base.data.Transaction;
import io.icw.base.signture.P2PHKSignature;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.pow.BlockPow;
import io.icw.rpc.AccountTools;
import io.icw.rpc.BlockCall;
import io.icw.rpc.CallHelper;
import io.icw.rpc.ChainTools;
import io.icw.rpc.LegderTools;
import io.icw.rpc.TransactionTools;
import io.icw.rpc.vo.Account;
import io.icw.rpc.vo.AccountBalance;

public class PowProcess {
	public static Config config = SpringLiteContext.getBean(Config.class);

	private LegderTools legderTools = SpringLiteContext.getBean(LegderTools.class);

	private TransactionTools transactionTools = SpringLiteContext.getBean(TransactionTools.class);
	
	private ChainTools chainTools = SpringLiteContext.getBean(ChainTools.class);

	private AccountTools accountTools = SpringLiteContext.getBean(AccountTools.class);
    
	private Map<Long, BlockPow> roundMap = new HashMap<Long, BlockPow>();

	PowAlgorithmThread powAlgorithmThread = new PowAlgorithmThread();
	
	private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	private static Cache<String, List<BlockPow>> membersCache = CacheUtil.newFIFOCache(1);
	
	private static Cache<Long, Long> diffCache = CacheUtil.newFIFOCache(100);
	
	private static Cache<Long, Block> blockCache = CacheUtil.newFIFOCache(100);
	
	private static long divideRound2 = 243550l;
	private static long divideRound3 = 243800l;
	public static long divideRound4 =  249532l;
	
	public static void clearCahce() {
		membersCache.clear();
		diffCache.clear();
		blockCache.clear();
		Log.info("clearCahce---------------------------");
	}
	
	public static List<String> getRoundMembersByHeightIndex(long round, long blockHeight) {
		round = round / config.getRound();
		return getRoundMembersByHeight(round, blockHeight);
	}
	
	public static List<String> getRoundMembersByHeight(long round, long blockHeight) {
		return getRoundMembersByHeight(round, blockHeight, false);
	}
	
	public static List<String> getRoundMembersByHeight(long round, long blockHeight, boolean isReal) {
		Log.info("round: " + round + " blockHeight: " + blockHeight);
		if (round == divideRound4) {
			clearCahce();
			return getRoundMembersByHeightV4(round, blockHeight, isReal);
		}
		if (round > divideRound4) {
			return getRoundMembersByHeightV4(round, blockHeight, isReal);
		}
		if (round > divideRound3) {
			return getRoundMembersByHeightV3(round, blockHeight);
		}
		if (round > divideRound2) {
			return getRoundMembersByHeightV2(round, blockHeight);
		}
		return getRoundMembersByHeightV1(round, blockHeight);
	}
	
	public static List<String> getRoundMembersByHeightV1(long round, long blockHeight) {
		List<String> members = new ArrayList<String>();
		BlockCall blockCall = new BlockCall();
		while (true) {
			Block block = null;
			try {
				block = blockCall.getBlockByHeight(config.getChainId(), blockHeight--);
			} catch (Exception e) {
				Log.error("fork?" + e);
				continue;
			}
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
			if (bRound == round - 1) {
				List<Transaction> txs = block.getTxs();
				List<String> txMembers = new ArrayList<String>();
				for (Transaction tx : txs) {
					if (tx.getType() == Constant.TX_TYPE_POW) {
						BlockPow blockPow = new BlockPow();
				        try {
							blockPow.parse(new NulsByteBuffer(tx.getTxData()));
							if (round == blockPow.getIndex()) {
								txMembers.add(blockPow.getAddress());
							} else {
								Log.info("getRoundMembersByHeight blockPow: " + blockPow);
							}
						} catch (NulsException e) {
							e.printStackTrace();
						}
					}
				}
				Collections.reverse(txMembers);
				members.addAll(txMembers);
			}
			if (bRound < round - 1) {
				break;
			}
		}
		Collections.reverse(members);
		
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < members.size() && i < config.getPackNumber(); i++) {
			ret.add(members.get(i));
		}
		
		Log.info("round: " + round + " blockHeight: " + blockHeight + " members: " + ret);
		return ret;
	}
	
	public static List<String> getRoundMembersByHeightV2(long round, long blockHeight) {
		long startTime = System.currentTimeMillis();
		List<String> members = new ArrayList<String>();

		BlockCall blockCall = new BlockCall();
		long maxDiff = 0;
		blockCache.clear();
		while (true) {
			Block block = null;
			try {
				block = blockCall.getBlockByHeight(config.getChainId(), blockHeight);
				blockHeight--;
			} catch (Exception e) {
				blockHeight--;
				Log.error("fork?" + e);
				continue;
			}
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
			if (bRound == round - 1) {
				List<Transaction> txs = block.getTxs();
				List<String> txMembers = new ArrayList<String>();
				for (Transaction tx : txs) {
					if (tx.getType() == Constant.TX_TYPE_POW) {
						BlockPow blockPow = new BlockPow();
				        try {
							blockPow.parse(new NulsByteBuffer(tx.getTxData()));
							Block powBlock = blockCache.get(blockPow.getHeight());
							if (powBlock == null) {
								powBlock = blockCall.getBlockByHeight(config.getChainId(), blockPow.getHeight());
								blockCache.put(blockPow.getHeight(), powBlock);
							}
							BlockExtendsData powExtendsData = powBlock.getHeader().getExtendsData();
							if (powExtendsData.getRoundIndex() / config.getRound() == round - 1 
									&& powBlock.getHeader().getHash().toString().equals(blockPow.getPreHash())
									&& round == blockPow.getIndex()) {
								Log.info("round: " + round + " :: getRoundMembersByHeight blockPow: " + blockPow);
							} else {
								Log.error("round: " + round + " :: getRoundMembersByHeight blockPow: " + blockPow);
								continue;
							}
							long bDiff = blockPow.getDiff();
							if (bDiff > maxDiff) {
								maxDiff = bDiff;
								txMembers.clear();
								members.clear();
								txMembers.add(blockPow.getAddress());
							} else if (bDiff == maxDiff){
								if (!txMembers.contains(blockPow.getAddress())) {
									txMembers.add(blockPow.getAddress());
								}
							} else {
								Log.error("maxDiff: " + maxDiff + " :: getRoundMembersByHeight blockPow: " + blockPow);
							}
						} catch (NulsException e) {
							Log.error("getRoundMembersByHeightV2", e);
						}
					}
				}
				Collections.reverse(txMembers);
				members.addAll(txMembers);
			}
			if (bRound < round - 1) {
				break;
			}
		}
		Collections.reverse(members);
		
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < members.size() && i < config.getPackNumber(); i++) {
			ret.add(members.get(i));
		}
		long endTime = System.currentTimeMillis();
		Log.info("cost: " + (endTime - startTime) + " round: " + round + " blockHeight: " + blockHeight + " members: " + ret);
		return ret;
	}
	
	public static List<String> getRoundMembersByHeightV3(long round, long blockHeight) {
		long startTime = System.currentTimeMillis();
		List<String> members = new ArrayList<String>();
		BlockCall blockCall = new BlockCall();
		blockCache.clear();
		Map<String, String> hashCodeMap = new HashMap<String, String>();
		while (true) {
			Block block = null;
			try {
				block = blockCall.getBlockByHeight(config.getChainId(), blockHeight);
				blockHeight--;
			} catch (Exception e) {
				blockHeight--;
				Log.error("fork?" + e);
				continue;
			}
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
			if (bRound == round - 1) {
				List<Transaction> txs = block.getTxs();
				List<String> txMembers = new ArrayList<String>();
				for (Transaction tx : txs) {
					if (tx.getType() == Constant.TX_TYPE_POW) {
						BlockPow blockPow = new BlockPow();
				        try {
							blockPow.parse(new NulsByteBuffer(tx.getTxData()));
							Block powBlock = blockCache.get(blockPow.getHeight());
							if (powBlock == null) {
								powBlock = blockCall.getBlockByHeight(config.getChainId(), blockPow.getHeight());
								blockCache.put(blockPow.getHeight(), powBlock);
							}
							BlockHeader header = powBlock.getHeader();
							BlockExtendsData powExtendsData = powBlock.getHeader().getExtendsData();

							Log.info(header.getHash().toString() + "=" + blockPow.getPreHash());
							Log.info(blockPow.generationHashCodeBySha256() + "=" + (blockPow.getHashCode()));
							Log.info(String.valueOf(header.getExtendsData().getRoundIndex() / PowProcess.config.getRound()) 
									+ "=" + String.valueOf(round - 1));
							Log.info(String.valueOf((header.getExtendsData().getRoundIndex() - 1) / config.getRound()) 
									+ "=" + String.valueOf(round - 2));
							Log.info(blockPow.getIndex() + "=" + round);
							
							if (header.getHash().toString().equals(blockPow.getPreHash())
									&& blockPow.generationHashCodeBySha256().equals(blockPow.getHashCode())
									&& header.getExtendsData().getRoundIndex() / config.getRound() == round - 1
									&& (header.getExtendsData().getRoundIndex() - 1) / config.getRound() == round - 2
									&& blockPow.getIndex() == round) {
								if (hashCodeMap.containsKey(blockPow.getHashCode())) {
									txMembers.remove(hashCodeMap.get(blockPow.getHashCode()));
									members.remove(hashCodeMap.get(blockPow.getHashCode()));
								} else {
									hashCodeMap.put(blockPow.getHashCode(), blockPow.getAddress());
								}
								txMembers.add(blockPow.getAddress());
//								Log.info("round ok: " + round + " :: getRoundMembersByHeight blockPow: " + blockPow);
							} else {
//								Log.error("round err: " + round + " :: getRoundMembersByHeight blockPow: " + blockPow);
								continue;
							}
						} catch (NulsException e) {
							Log.error("getRoundMembersByHeightV3", e);
						}
					}
				}
				Collections.reverse(txMembers);
				members.addAll(txMembers);
			}
			if (bRound < round - 1) {
				break;
			}
		}
		Collections.reverse(members);
		
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < members.size() && i < config.getPackNumber(); i++) {
			ret.add(members.get(i));
		}
		long endTime = System.currentTimeMillis();
		Log.info("cost: " + (endTime - startTime) + " round: " + round + " blockHeight: " + blockHeight + " members: " + ret);
		return ret;
	}
	
	public static List<String> getRoundMembersByHeightV4(long round, long blockHeight, boolean isReal) {
		long startTime = System.currentTimeMillis();
		long diff = getCalculateDiffV4(round, blockHeight);
		
		List<String> members = new ArrayList<String>();
		BlockCall blockCall = new BlockCall();
		Map<String, String> hashCodeMap = new HashMap<String, String>();
		while (true) {
			Block block = null;
			try {
				block = blockCache.get(blockHeight);
				if (block == null) { 
					block = blockCall.getBlockByHeight(config.getChainId(), blockHeight);
					blockCache.put(blockHeight, block);
				}
				blockHeight--;
			} catch (Exception e) {
				blockHeight--;
				Log.error("fork?" + e);
				continue;
			}
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
			if (bRound == round - 1) {
				List<Transaction> txs = block.getTxs();
				List<String> txMembers = new ArrayList<String>();
				for (Transaction tx : txs) {
					if (tx.getType() == Constant.TX_TYPE_POW) {
						BlockPow blockPow = new BlockPow();
				        try {
							blockPow.parse(new NulsByteBuffer(tx.getTxData()));
							Block powBlock = blockCache.get(blockPow.getHeight());
							if (powBlock == null) {
								powBlock = blockCall.getBlockByHeight(config.getChainId(), blockPow.getHeight());
								blockCache.put(blockPow.getHeight(), powBlock);
							}
							BlockHeader header = powBlock.getHeader();
//							BlockExtendsData powExtendsData = powBlock.getHeader().getExtendsData();
							
							String pre = "";
							for (int i = 0; i < blockPow.getDiff(); i++) {
								pre = pre + "0";
							}
							
//							Log.info(header.getHash().toString() + "=" + blockPow.getPreHash());
//							Log.info(blockPow.generationHashCodeBySha256() + "=" + (blockPow.getHashCode()));
//							Log.info(String.valueOf(header.getExtendsData().getRoundIndex() / PowProcess.config.getRound()) 
//									+ "=" + String.valueOf(round - 1));
//							Log.info(String.valueOf((header.getExtendsData().getRoundIndex() - 1) / config.getRound()) 
//									+ "=" + String.valueOf(round - 2));
//							Log.info(blockPow.getIndex() + "=" + round);
//							Log.info(blockPow.getDiff() + "=" + diff);
//							Log.info(String.valueOf(blockPow.getHashCode().startsWith(pre)));
							
							if (header.getHash().toString().equals(blockPow.getPreHash())
									&& blockPow.generationHashCodeBySha256().equals(blockPow.getHashCode())
									&& header.getExtendsData().getRoundIndex() / config.getRound() == round - 1
									&& (header.getExtendsData().getRoundIndex() - 1) / config.getRound() == round - 2
									&& blockPow.getIndex() == round
									&& blockPow.getDiff() == diff
									&& blockPow.getHashCode().startsWith(pre)) {
								if (hashCodeMap.containsKey(blockPow.getHashCode())) {
									txMembers.remove(hashCodeMap.get(blockPow.getHashCode()));
									members.remove(hashCodeMap.get(blockPow.getHashCode()));
								} else {
									hashCodeMap.put(blockPow.getHashCode(), blockPow.getAddress());
								}
								txMembers.add(blockPow.getAddress());
//								Log.info("round ok: " + round + " :: getRoundMembersByHeight blockPow: " + blockPow);
							} else {
//								Log.error("round err: " + round + " :: getRoundMembersByHeight blockPow: " + blockPow);
								continue;
							}
						} catch (Exception e) {
							Log.error("getRoundMembersByHeightV4:" + blockPow, e);
						}
					}
				}
				Collections.reverse(txMembers);
				members.addAll(txMembers);
			}
			if (bRound < round - 1) {
				break;
			}
		}
		Collections.reverse(members);
		
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < members.size() && i < config.getPackNumber(); i++) {
			ret.add(members.get(i));
		}
		
		if (!isReal) {
			if (ret.size() != config.getPackNumber()) {
				try {
					Map configMap = CallHelper.getConsensusConfig(config.getChainId());
			        String seeds = configMap.get("seedNodes").toString();
			        List<String> seedNodes = new ArrayList<>();
			        for (String seed : seeds.split(",")) {
			            seedNodes.add(seed);
			        }
			        seedNodes.removeAll(ret);
			        if (config.getPackNumber() - ret.size() - seedNodes.size() >= 0) {
			        	ret.addAll(seedNodes);
			        } else {
			        	ret.addAll(seedNodes.subList(0, config.getPackNumber() - ret.size()));
			        }
				} catch (NulsException e) {
					Log.error(e);
				}
			}
		}
		
		long endTime = System.currentTimeMillis();
		Log.info("cost: " + (endTime - startTime) + " round: " + round + " blockHeight: " + blockHeight + " members: " + ret);
		return ret;
	}

	public static List<BlockPow> getRoundMembers(long round, long blockHeight) {
		String key = round + "_" ;//+ blockHeight;
		List<BlockPow> members = membersCache.get(key);
		if (members != null) {
			return members;
		}
		long startTime = System.currentTimeMillis();
		members = new ArrayList<BlockPow>();
		BlockCall blockCall = new BlockCall();
		while (true) {
			Block block = blockCall.getBlockByHeight(config.getChainId(), blockHeight--);
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
			if (bRound == round - 1) {
				List<Transaction> txs = block.getTxs();
				List<BlockPow> txMembers = new ArrayList<BlockPow>();
				for (Transaction tx : txs) {
					if (tx.getType() == Constant.TX_TYPE_POW) {
						BlockPow blockPow = new BlockPow();
				        try {
							blockPow.parse(new NulsByteBuffer(tx.getTxData()));
							if (round == blockPow.getIndex()) {
								txMembers.add(blockPow);
							}
						} catch (NulsException e) {
							e.printStackTrace();
						}
					}
				}
				Collections.reverse(txMembers);
				members.addAll(txMembers);
			}
			if (bRound < round - 1) {
				break;
			}
		}
		Collections.reverse(members);
//		Log.info("members: " + members);
		long endTime = System.currentTimeMillis();
		
		Log.info("getRoundMembers cost: " + members.size() + " : " + (endTime - startTime));
		membersCache.put(key, members);
		return members;
	}
	
	private static List<BlockPow> getRoundMembersV4(long round, long blockHeight) {
		long startTime = System.currentTimeMillis();
		
		List<BlockPow> members = new ArrayList<BlockPow>();
		BlockCall blockCall = new BlockCall();
		blockCache.clear();
		Map<String, BlockPow> hashCodeMap = new HashMap<String, BlockPow>();
		while (true) {
			Block block = null;
			try {
				block = blockCache.get(blockHeight);
				if (block == null) { 
					block = blockCall.getBlockByHeight(config.getChainId(), blockHeight);
					blockCache.put(blockHeight, block);
				}
				blockHeight--;
			} catch (Exception e) {
				blockHeight--;
				Log.error("fork?" + e);
				continue;
			}
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
			
//			Log.info("bRound:::::::: " + bRound + " blockHeight: " + blockHeight);
			
			if (bRound == round - 1) {
				List<Transaction> txs = block.getTxs();
				List<BlockPow> txMembers = new ArrayList<BlockPow>();
				for (Transaction tx : txs) {
					if (tx.getType() == Constant.TX_TYPE_POW) {
						BlockPow blockPow = new BlockPow();
				        try {
							blockPow.parse(new NulsByteBuffer(tx.getTxData()));
							Block powBlock = blockCache.get(blockPow.getHeight());
							if (powBlock == null) {
								powBlock = blockCall.getBlockByHeight(config.getChainId(), blockPow.getHeight());
								blockCache.put(blockPow.getHeight(), powBlock);
							}
							BlockHeader header = powBlock.getHeader();
							BlockExtendsData powExtendsData = powBlock.getHeader().getExtendsData();

							String pre = "";
							for (int i = 0; i < blockPow.getDiff(); i++) {
								pre = pre + "0";
							}
							
							if (header.getHash().toString().equals(blockPow.getPreHash())
									&& blockPow.generationHashCodeBySha256().equals(blockPow.getHashCode())
									&& header.getExtendsData().getRoundIndex() / config.getRound() == round - 1
									&& (header.getExtendsData().getRoundIndex() - 1) / config.getRound() == round - 2
									&& blockPow.getIndex() == round
									&& blockPow.getHashCode().startsWith(pre)) {
								if (hashCodeMap.containsKey(blockPow.getHashCode())) {
									txMembers.remove(hashCodeMap.get(blockPow.getHashCode()));
									members.remove(hashCodeMap.get(blockPow.getHashCode()));
								} else {
									hashCodeMap.put(blockPow.getHashCode(), blockPow);
								}
								txMembers.add(blockPow);
							} else {
								continue;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				Collections.reverse(txMembers);
				members.addAll(txMembers);
			}
			if (bRound < round - 1) {
				break;
			}
		}
		Collections.reverse(members);
		
		long endTime = System.currentTimeMillis();
		Log.info("cost: " + (endTime - startTime) + " round: " + round + " blockHeight: " + blockHeight + " members:" + members.size());
		return members;
	}

	public PowProcess() {
		config = SpringLiteContext.getBean(Config.class);
		singleThreadExecutor.execute(powAlgorithmThread);
	}
    
    public void process(int chainId) {
        try {
            boolean canPackage = true;
            if (!canPackage) {
                return;
            }
            
            doWork(chainId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getRoundTime() {
    	long time = config.getRound() * config.getPackNumber() * 6 * 1000;
    	return time;
    }
    
    public static long getCalculateDiff(long round, long blockHeight) {
    	long cacheKey = round;
    	
    	long diff = 0;
    	
    	Long diffLong = diffCache.get(round);
    	Log.info("diffLong=" + diffLong + "::" + round + "::" + blockHeight);

    	if (diffLong != null) {
    		diff = diffLong.longValue();
    		Log.info("diff=" + diff);
    		return diff;
    	}
    	
    	round--;
    	List<BlockPow> members = getRoundMembers(round, blockHeight);
    	if (!members.isEmpty()) {
    		for (BlockPow blockPow : members) {
    			if (blockPow.getDiff() > diff) {
    				diff = blockPow.getDiff();
    			}
    		}
    		Log.info("members size: " + members.size() + " max diff: " + diff);
		} else {
			for (diff = config.getDiff(); diff > 0; diff--) {
	    		round--;
	    		List<BlockPow> preMembers = getRoundMembers(round, blockHeight);
	    		if (!preMembers.isEmpty()) {
	    			diff = preMembers.get(0).getDiff();
	    			break;
	    		}
			}
		}
		
    	if (members.size() < config.getPackNumber()){
    		diff = diff - 1;
    		Log.info("diff=" + diff);
    	} else {
    		long time = 0;
    		for (BlockPow blockPow : members) {
    			time += blockPow.getTimestamp() - blockPow.getPowTimestamp();
    			diff = blockPow.getDiff();
    		}
    		double avgtime = time / members.size();
    		
    		Log.info("avgtime: " + avgtime + " getRoundTime: " + getRoundTime() + " members: " + members);
    		if (avgtime / getRoundTime() < 0.2) {
    			diff = diff + 1;
    			Log.info("diff=" + diff);
    		}
    		Log.info("diff=" + diff);
    	}
    	
    	diff = diff < 1 ? 1 : diff;
    	
    	
    	diffCache.put(cacheKey, diff);
//    	Log.info(cacheKey + "::" + diffCache.toString());

    	return diff;
    }
    
    public static long getCalculateDiffV4(long round, long blockHeight) {
//    	long cacheKey = round;
    	
    	long diff = 0;
    	
//    	Long diffLong = diffCache.get(round);
//    	Log.info("diffLong=" + diffLong + "::" + round + "::" + blockHeight);
//    	
//    	if (diffLong != null) {
//    		diff = diffLong.longValue();
//    		Log.info("diff=" + diff);
//    		return diff;
//    	}
    	
    	round--;
    	List<BlockPow> members = getRoundMembersV4(round, blockHeight);
    	if (!members.isEmpty()) {
    		for (BlockPow blockPow : members) {
    			if (blockPow.getDiff() > diff) {
    				diff = blockPow.getDiff();
    			}
    		}
    		Log.info("members size: " + members.size() + " max diff: " + diff);
		} else {
			diff = config.getDiff();
//			for (diff = config.getDiff(); diff > 0; diff--) {
//	    		round--;
//	    		List<BlockPow> preMembers = getRoundMembersV4(round, blockHeight);
//	    		if (!preMembers.isEmpty()) {
//	    			diff = preMembers.get(0).getDiff();
//	    			break;
//	    		}
//			}
		}
		
    	if (members.size() < config.getPackNumber()){
    		diff = diff - 1;
    		Log.info("diff=" + diff);
    	} else {
    		long time = 0;
    		for (BlockPow blockPow : members) {
    			time += blockPow.getTimestamp() - blockPow.getPowTimestamp();
    			diff = blockPow.getDiff();
    		}
    		double avgtime = time / members.size();
    		
    		Log.info("avgtime: " + avgtime); // + " members: " + members);
//    		if (avgtime / getRoundTime() < 0.2) {
    		if (avgtime < 24 * 1000) {
    			diff = diff + 1;
    			Log.info("diff=" + diff);
    		}
    		Log.info("diff=" + diff);
    	}
    	
    	diff = diff < 1 ? 1 : diff;
    	
    	
//    	diffCache.put(cacheKey, diff);

    	return diff;
    }

	private void doWork(int chainId) {
		try {
			BlockHeader blockHeader = BlockCall.getLatestBlockHeader( config.getChainId() );
			long roundIndex = blockHeader.getExtendsData().getRoundIndex();
			long round = roundIndex / config.getRound();
			round++;
			
			if (roundMap.get(round) == null) {
				Map<String, String> addressMap = chainTools.getPackingAddress(chainId);
				if (addressMap.isEmpty()) {
					return;
				}
				String address = addressMap.get("address");
				//TODO is seed?
				if (address == null) {
					return;
				}
				Map agentAddress = chainTools.getAgentAddressList(chainId);
				List<String> packAddress = (List<String>)agentAddress.get("packAddress");
				if (packAddress != null && packAddress.size() > 50) {
					List<String> members = getRoundMembersByHeight(round - 1, blockHeader.getHeight(), true);
					if (members.contains(address)) {
						return;
					}
				}
				
				powAlgorithmThread.setBlockHeader(round, blockHeader, address);

				BlockPow blockPow = powAlgorithmThread.getBlockPow(round);
				if (blockPow != null) {
					Log.info("round: " + round + " blockPow: " + blockPow);
				}
				
				if (blockPow != null && roundMap.get(round) == null) {
					try {
						int stop = config.getStop();
				        if (stop != 1) {
							String password = addressMap.get("password");
							accountTools.accountValid(Integer.valueOf( config.getChainId() ).intValue(), address, password);
				            Account account = accountTools.getAccountByAddress(address);
				            
							Transaction tx = new Transaction();
					        tx.setType(Constant.TX_TYPE_POW);
					        tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
					        tx.setTxData(blockPow.serialize());
					        tx.setCoinData(buildCoinData(tx, AddressTool.getAddress(account.getAddress())));
					        
					        signTransaction(tx, account, password);
					        transactionTools.newTx(tx);
				        }
					} catch (Exception e) {
						Log.error(e);
					}
			        roundMap.clear();
			        roundMap.put(round, blockPow);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private byte[] buildCoinData(Transaction tx,byte[] senderAddress) throws IOException, NulsException {
    	AccountBalance accountBalance = legderTools.getBalanceAndNonce(config.getChainId(), AddressTool.getStringAddressByBytes(senderAddress), config.getChainId(), config.getAssetId());
        byte locked = 0;
        byte[] nonce = RPCUtil.decode(accountBalance.getNonce());
        CoinFrom coinFrom = new CoinFrom(senderAddress, config.getChainId(), config.getAssetId(), config.getPowFee(), nonce, locked);
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(Constant.BLACK_HOLE_ADDRESS), config.getChainId(), config.getAssetId(), config.getPowFee());
        int txSize = tx.size() + coinFrom.size() + coinTo.size() + P2PHKSignature.SERIALIZE_LENGTH;
        //计算手续费
        BigInteger fee = BigInteger.valueOf(0); //TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = config.getPowFee().add(fee);
//        if(accountBalance.getAvailable().min(config.getPowFee().add(tx.getFee())).equals(accountBalance.getAvailable())){
//            throw new NulsRuntimeException(CommonCodeConstanst.FAILED, "insufficient fee");
//        }
        coinFrom.setAmount(totalAmount);
        CoinData coinData = new CoinData();
        coinData.setFrom(List.of(coinFrom));
        coinData.setTo(List.of(coinTo));
        return coinData.serialize();
    }
    
    private Transaction signTransaction(Transaction transaction, Account account, String password) throws IOException {
        return Utils.signTransaction(transaction,account.getEncryptedPrikeyHex(),account.getPubkeyHex(),password);
    }
}

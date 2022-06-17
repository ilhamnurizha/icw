package io.icw.thread.process;

import io.icw.Config;
import io.icw.Constant;
import io.icw.Utils;
import io.icw.base.RPCUtil;
import io.icw.base.basic.AddressTool;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.*;
import io.icw.base.signture.P2PHKSignature;
import io.icw.core.core.ioc.SpringLiteContext;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.core.rpc.util.NulsDateUtils;
import io.icw.pow.BlockPow;
import io.icw.rpc.*;
import io.icw.rpc.vo.Account;
import io.icw.rpc.vo.AccountBalance;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PowProcess {
	public static Config config = SpringLiteContext.getBean(Config.class);

	private LegderTools legderTools = SpringLiteContext.getBean(LegderTools.class);

	private TransactionTools transactionTools = SpringLiteContext.getBean(TransactionTools.class);
	
	private ChainTools chainTools = SpringLiteContext.getBean(ChainTools.class);

	private AccountTools accountTools = SpringLiteContext.getBean(AccountTools.class);
    
	private Map roundMap = new HashMap();

	PowAlgorithmThread powAlgorithmThread = new PowAlgorithmThread();
	
	private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	private static Map<Long, List> roundMemberMap = new HashMap<Long, List>();
	
	private static Map<String, List> membersCacheMap = new HashMap<String, List>();
	
//	public static void addRoundMember(long round, String address) {
//		List<String> members = roundMemberMap.get(round);
//		if (members == null) {
//			members = new ArrayList<String>();
//			roundMemberMap.put(round, members);
//		}
//		if (!members.contains(address)) {
//			members.add(address);
//		}
//	}
//
//	public static List<String> getRoundMembers(long round) {
//		Log.info(roundMemberMap.toString());
//		round = round / config.getRound();
//		List<String> members = roundMemberMap.get(round);
//		if (members == null) {
//			members = new ArrayList<String>();
//		}
//		
//		List<String> ret = new ArrayList<String>();
//		for (int i = 0; i < members.size() && i < config.getPackNumber(); i++) {
//			ret.add(members.get(i));
//		}
//		
//		clearRoundMembers(round);
//		
//		return ret;
//	}
	
	public static List<String> getRoundMembersByHeightIndex(long round, long blockHeight) {
		round = round / config.getRound();
		return getRoundMembersByHeight(round, blockHeight);
	}
	
	public static List<String> getRoundMembersByHeight(long round, long blockHeight) {
		List<String> members = new ArrayList<String>();
		BlockCall blockCall = new BlockCall();
		while (true) {
			Block block = blockCall.getBlockByHeight(config.getChainId(), blockHeight--);
			BlockExtendsData extendsData = block.getHeader().getExtendsData();
			long bRound = extendsData.getRoundIndex() / config.getRound();
//			Log.info("round: " + round + " bRound: " + bRound + " blockHeight: " + blockHeight);
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
		
//		Log.info("round: " + round + " blockHeight: " + blockHeight + " members: " + members);
		return ret;
	}
	
	public static List<BlockPow> getRoundMembers(long round, long blockHeight) {
		String key = round + "_" + blockHeight;
		List<BlockPow> members = membersCacheMap.get(key);
		if (members != null) {
			membersCacheMap.clear();
			membersCacheMap.put(key, members);
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
		
//		Log.info("getRoundMembers cost: " + members.size() + " : " + (endTime - startTime));
		membersCacheMap.put(key, members);
		return members;
	}
	
	public static void clearRoundMembers(long round) {
		Iterator<Long> it = roundMemberMap.keySet().iterator();
		List<Long> rmKeys = new ArrayList<Long>();
		while (it.hasNext()) {
			long roundTmp = it.next();
			if (roundTmp < round - 1) {
				rmKeys.add(roundTmp);
			}
		}
		for (long key : rmKeys) {
			roundMemberMap.remove(key);
		}
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
    	round--;
    	long diff = 0;
    	List<BlockPow> members = getRoundMembers(round, blockHeight);
    	if (!members.isEmpty()) {
			diff = members.get(0).getDiff();
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
    	}
    	
    	diff = diff < 1 ? 1 : diff;
    	
    	return diff;
    }
    
	private void doWork(int chainId) {
//		Log.info("doWork = " + chainId);
		try {
//			MeetingRound meetingRound = chainTools.getRoundInfo(chainId);
//			Log.info("meetingRound: " + meetingRound);
			BlockHeader blockHeader = BlockCall.getLatestBlockHeader( config.getChainId() );
			long roundIndex = blockHeader.getExtendsData().getRoundIndex();
			long round = roundIndex / config.getRound();
			round++;
//			Log.info("roundIndex=" + roundIndex + " round=" + round);
			
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
					List<String> members = getRoundMembersByHeight(round - 1, blockHeader.getHeight());
					if (members.contains(address)) {
						return;
					}
				}
				
//				BlockHeader blockHeader = BlockCall.getLatestBlockHeader( config.getChainId() );
				powAlgorithmThread.setBlockHeader(round, blockHeader, address);
//				BlockExtendsData extendsData = blockHeader.getExtendsData();
//				long roundIndex = extendsData.getRoundIndex();
				BlockPow blockPow = powAlgorithmThread.getBlockPow(round);
				if (blockPow != null) {
					Log.info("round: " + round + " blockPow: " + blockPow);
				}
				if (blockPow != null && roundMap.get(round) == null) {
					try {
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

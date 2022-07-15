package io.icw.txhander;

import io.icw.Config;
import io.icw.Constant;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.NulsHash;
import io.icw.base.data.Transaction;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.core.exception.NulsException;
import io.icw.core.log.Log;
import io.icw.pow.BlockPow;
import io.icw.rpc.BlockCall;
import io.icw.rpc.ChainTools;
import io.icw.rpc.LegderTools;
import io.icw.thread.process.PowProcess;

@Component
public class PowProcessor implements TransactionProcessor {
    @Autowired
    LegderTools legderTools;
    
    @Autowired
    ChainTools chainTools; 
    
    @Autowired
    Config config;
    
//    private static Cache<String, String> powHashCache = CacheUtil.newFIFOCache(1000);
    
    @Override
    public int getType() {
        return Constant.TX_TYPE_POW;
    }

    @Override
    public boolean validate(int chainId, Transaction tx, BlockHeader blockHeader) {
//    	long tx1 =  System.currentTimeMillis();
        try {
//        	BlockPow blockPow = new BlockPow();
//			blockPow.parse(new NulsByteBuffer(tx.getTxData()));
			
//			Log.info("validate tx: " + blockPow);
			
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
			
            return true;
//			if ("ICWc6HgUREDTjzhkhBMywRoWjpoM1bdZQhiQ".equals(blockPow.getAddress())) {
//				return true;
//			}
//			if (blockPow.getHeight() < 2000000L) {
//            	return true;
//            }

			//理想很丰满 现实很骨感 达不成共识
//			String cachePow = powHashCache.get(blockPow.getHashCode());
//			if (cachePow != null && !cachePow.equals( tx.getHash().toString() )) {
//				Log.error("cachePow: " + cachePow + ":::" + tx.getHash().toString() + ":::" + blockPow);
//				return false;
//			} else {
//				powHashCache.put(blockPow.getHashCode(), tx.getHash().toString());
//			}
			
//			long roundindex = 0l;
//			if (blockHeader != null) {
//				roundindex = blockHeader.getExtendsData().getRoundIndex();
//			} else {
//				roundindex = chainTools.getRoundInfo(chainId).getIndex();
//			}
//			BlockHeader header = null;
//			try {
//				header = BlockCall.getBlockHeader(chainId, blockPow.getHeight());
//			} catch (Exception e) {
//				Log.error(e);
//				//分叉。。。。
//				return true;
//			}
//			long tx2 =  System.currentTimeMillis();
//			Log.info("validate tx2: " + tx2 + ":::" + (tx2 - tx1));
//			Log.debug("validate: " + header.getHash().toString() 
//					+ " : " + blockPow.getPreHash()
//					+ " : " + blockPow.generationHashCodeBySha256()
//					+ " : " + blockPow.getHashCode()
//					+ " : " + header.getExtendsData().getRoundIndex()
//					+ " : " + roundindex
//					+ " : " + blockPow.getIndex()
//					+ " : " + blockPow.getDiff()
//					+ " : " + blockPow.getTimestamp()
//					+ " : " + blockPow.getPowTimestamp()
//					+ " : " + (blockHeader != null ? blockHeader.getTime() : System.currentTimeMillis())
//					+ " : " + blockPow.getAddress()
//					+ " : " + (blockHeader != null ? AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chainId)) : "")
//					+ " : " + PowProcess.getCalculateDiff(blockPow.getIndex(), header.getHeight()));
			
//			String pre = "";
//			for (int i = 0; i < blockPow.getDiff(); i++) {
//				pre = pre + "0";
//			}
			
//			Map agentAddress = chainTools.getAgentAddressList(chainId);
//			List<String> packAddress = (List<String>)agentAddress.get("packAddress");
//			if (packAddress != null && packAddress.size() > 50) {
//				List<String> members = PowProcess.getRoundMembersByHeight(blockPow.getIndex() - 1, header.getHeight());
//				if (members.contains(blockPow.getAddress())) {
//					return false;
//				}
//			}
			
//			long tx4 =  System.currentTimeMillis();
////			Log.info("validate tx4: " + tx4 + ":::" + (tx4 - tx3));
//			long diff = PowProcess.getCalculateDiff(blockPow.getIndex(), header.getHeight());
//			long tx5 =  System.currentTimeMillis();
//			Log.info("validate tx5: " + tx5 + ":::" + (tx5 - tx4) + ":::" + blockPow.getIndex() + ":::" + header.getHeight());
//			
//			Log.info(header.getHash().toString() + "=" + blockPow.getPreHash());
//			Log.info(blockPow.generationHashCodeBySha256() + "=" + (blockPow.getHashCode()));
//			Log.info(header.getExtendsData().getRoundIndex() / PowProcess.config.getRound() + "=" + roundindex / PowProcess.config.getRound());
//			Log.info(String.valueOf((header.getExtendsData().getRoundIndex() - 1) / PowProcess.config.getRound()) + "=" 
//					+ String.valueOf(roundindex / PowProcess.config.getRound() - 1));
//			Log.info(roundindex / PowProcess.config.getRound() + 1 + "=" + blockPow.getIndex());
//			Log.info(blockPow.getDiff() + "=" + diff);
//			Log.info(String.valueOf(blockPow.getHashCode().startsWith(pre)));
//			
//			if (header.getHash().toString().equals(blockPow.getPreHash())
//					&& blockPow.generationHashCodeBySha256().equals(blockPow.getHashCode())
//					&& header.getExtendsData().getRoundIndex() / PowProcess.config.getRound() == roundindex / PowProcess.config.getRound()
//					&& (header.getExtendsData().getRoundIndex() - 1) / PowProcess.config.getRound() == roundindex / PowProcess.config.getRound() - 1
//					&& roundindex / PowProcess.config.getRound() + 1 == blockPow.getIndex()
//					&& blockPow.getDiff() == diff
//					&& blockPow.getHashCode().startsWith(pre)) {
////				long tx6 =  System.currentTimeMillis();
////				Log.info("validate tx6: " + tx6 + ":::" + (tx6 - tx5));
//				Log.info("validate tx: " + true);
//				return true;
//			}
////			long tx6 =  System.currentTimeMillis();
////			Log.info("validate tx6: " + tx6 + ":::" + (tx6 - tx5));
//			Log.info("validate tx: " + false);
//			return false;
        }catch (Throwable e){
            e.printStackTrace();
            Log.info(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean commit(int chainId, Transaction tx, BlockHeader blockHeader) {
//        Log.info("commit tx: " + tx);
//        Log.info("commit txData: " + tx.getTxData());
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
            
//            BlockPow blockPow = new BlockPow();
//			blockPow.parse(new NulsByteBuffer(tx.getTxData()));
//			
//			Log.info("commit tx: " + blockPow);
			
//            BlockPow blockPow = new BlockPow();
//			blockPow.parse(new NulsByteBuffer(tx.getTxData()));
////			Log.info("blockPow: " + blockPow);
//			BlockHeader header = BlockCall.getBlockHeader(chainId, blockPow.getHeight());
//			Log.info("commit: " + header.getHash().toString() 
//					+ " : " + blockPow.getPreHash()
//					+ " : " + blockPow.generationHashCodeBySha256()
//					+ " : " + blockPow.getHashCode()
//					+ " : " + header.getExtendsData().getRoundIndex()
//					+ " : " + blockHeader.getExtendsData().getRoundIndex()
//					+ " : " + blockPow.getIndex()
//					+ " : " + blockPow.getDiff()
//					+ " : " + blockPow.getTimestamp()
//					+ " : " + blockPow.getPowTimestamp()
//					+ " : " + blockHeader.getTime()
//					+ " : " + blockPow.getAddress()
//					+ " : " + AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chainId))
//					+ " : " + PowProcess.getCalculateDiff(blockPow.getIndex(), header.getHeight()));
//			
//			String pre = "";
//			for (int i = 0; i < config.getDiff(); i++) {
//				pre = pre + "0";
//			}
//			if (header.getHash().toString().equals(blockPow.getPreHash())
//					&& blockPow.generationHashCodeBySha256().equals(blockPow.getHashCode())
//					&& header.getExtendsData().getRoundIndex() / PowProcess.config.getRound() + 1 == blockHeader.getExtendsData().getRoundIndex() / PowProcess.config.getRound()
//					&& (header.getExtendsData().getRoundIndex() + 1) / PowProcess.config.getRound() == blockHeader.getExtendsData().getRoundIndex() / PowProcess.config.getRound()
//					&& blockHeader.getExtendsData().getRoundIndex() / PowProcess.config.getRound() + 1 == blockPow.getIndex()
//					&& blockPow.getDiff() == PowProcess.getCalculateDiff(blockPow.getIndex(), header.getHeight())
//					&& blockPow.getHashCode().startsWith(pre)) {
////				PowProcess.addRoundMember(blockPow.getIndex(), blockPow.getAddress());
//				Log.info("commit tx: " + true);
//				return true;
//			}
			return true;
        }catch (Throwable e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rollback(int chainId, Transaction tx, BlockHeader blockHeader) {
//        BlockPow blockPow = new BlockPow();
//		try {
//			blockPow.parse(new NulsByteBuffer(tx.getTxData()));
//		} catch (NulsException e) {
//			e.printStackTrace();
//		}
//		Log.info("rollback tx: " + blockPow);
		
        PowProcess.clearCahce();
        
        return true;
    }
}

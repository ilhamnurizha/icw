package io.icw.txhander;

import java.util.List;
import java.util.Map;

import io.icw.Config;
import io.icw.Constant;
import io.icw.base.basic.AddressTool;
import io.icw.base.basic.NulsByteBuffer;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.NulsHash;
import io.icw.base.data.Transaction;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
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

    @Override
    public int getType() {
        return Constant.TX_TYPE_POW;
    }

    @Override
    public boolean validate(int chainId, Transaction tx, BlockHeader blockHeader) {
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
            
            BlockPow blockPow = new BlockPow();
			blockPow.parse(new NulsByteBuffer(tx.getTxData()));
			
			long roundindex = 0l;
			if (blockHeader != null) {
				roundindex = blockHeader.getExtendsData().getRoundIndex();
			} else {
				roundindex = chainTools.getRoundInfo(chainId).getIndex();
			}
			BlockHeader header = BlockCall.getBlockHeader(chainId, blockPow.getHeight());
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
			
			String pre = "";
			for (int i = 0; i < blockPow.getDiff(); i++) {
				pre = pre + "0";
			}
			
			Map agentAddress = chainTools.getAgentAddressList(chainId);
			List<String> packAddress = (List<String>)agentAddress.get("packAddress");
			if (packAddress != null && packAddress.size() > 50) {
				List<String> members = PowProcess.getRoundMembersByHeight(blockPow.getIndex() - 1, header.getHeight());
				if (members.contains(blockPow.getAddress())) {
					return false;
				}
			}
			
			if (header.getHash().toString().equals(blockPow.getPreHash())
					&& blockPow.generationHashCodeBySha256().equals(blockPow.getHashCode())
					&& header.getExtendsData().getRoundIndex() / PowProcess.config.getRound() == roundindex / PowProcess.config.getRound()
					&& (header.getExtendsData().getRoundIndex() - 1) / PowProcess.config.getRound() == roundindex / PowProcess.config.getRound() - 1
					&& roundindex / PowProcess.config.getRound() + 1 == blockPow.getIndex()
					&& blockPow.getDiff() == PowProcess.getCalculateDiff(blockPow.getIndex(), header.getHeight())
					&& blockPow.getHashCode().startsWith(pre)) {
				Log.info("validate tx: " + true);
				return true;
			}
			Log.info("validate tx: " + false);
			return false;
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
        Log.info("rollback tx");
        return false;
    }
}

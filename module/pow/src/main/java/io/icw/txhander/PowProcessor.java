package io.icw.txhander;

import io.icw.Constant;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.NulsHash;
import io.icw.base.data.Transaction;
import io.icw.core.core.annotation.Component;
import io.icw.core.log.Log;
import io.icw.thread.process.PowProcess;

@Component
public class PowProcessor implements TransactionProcessor {
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
            return true;
        }catch (Throwable e){
            e.printStackTrace();
            Log.info(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean commit(int chainId, Transaction tx, BlockHeader blockHeader) {
        try {
            NulsHash nulsHash = NulsHash.calcHash(tx.serializeForHash());
            if (!nulsHash.equals(tx.getHash())) {
                return false;
            }
			return true;
        }catch (Throwable e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rollback(int chainId, Transaction tx, BlockHeader blockHeader) {
    	Log.info("rollback ---------- ");
    	
        PowProcess.clearCahce();
        
        return true;
    }
}

package io.icw.crosschain.base.tx.v1;

import com.google.common.collect.Maps;
import io.icw.base.data.BlockHeader;
import io.icw.base.data.Transaction;
import io.icw.base.protocol.TransactionProcessor;
import io.icw.core.constant.TxType;
import io.icw.core.core.annotation.Autowired;
import io.icw.core.core.annotation.Component;
import io.icw.crosschain.base.service.ResetLocalVerifierService;

import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2020/11/23 12:00
 * @Description: 功能描述
 */
@Component("ResetLocalVerifierProcessorV1")
public class ResetLocalVerifierProcessor implements TransactionProcessor {

    @Autowired
    ResetLocalVerifierService resetLocalVerifierService;

    @Override
    public int getType() {
        return TxType.RESET_LOCAL_VERIFIER_LIST;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        if (txs.isEmpty()) {
            Map<String,Object> result = Maps.newHashMap();
            result.put("txList", txs);
            result.put("errorCode", null);
            return result;
        }
        return resetLocalVerifierService.validate(chainId, txs, blockHeader);
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        if(txs.isEmpty()){
            return true;
        }
        return resetLocalVerifierService.commitTx(chainId, txs, blockHeader);
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        if(txs.isEmpty()){
            return true;
        }
        return resetLocalVerifierService.rollbackTx(chainId, txs, blockHeader);
    }

}
